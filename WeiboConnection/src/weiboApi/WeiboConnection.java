package weiboApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import weibo4j.Account;
import weibo4j.Comments;
import weibo4j.Friendships;
import weibo4j.ShortUrl;
import weibo4j.Users;
import weibo4j.weibot.Geo;
import weibo4j.weibot.GeoPosition;
import weibo4j.Oauth;
import weibo4j.Timeline;
import weibo4j.weibot.PlaceTimeline;
import weibo4j.model.Comment;
import weibo4j.model.CommentWapper;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.User;
import weibo4j.model.WeiboException;
import weibo4j.weibot.DirectMessage;
import weibo4j.weibot.DirectMessageWapper;
import weibo4j.weibot.DirectMessages;
import weibo4j.org.json.JSONObject;
import weibo4j.util.BareBonesBrowserLaunch;
import org.apache.log4j.Logger;

import weibot.MsgQueue;

public class WeiboConnection {
	private static Logger connlogger = Logger.getLogger(WeiboConnection.class);

	String regexp = "(((http|ftp|https|file)://)|((?<!((http|ftp|https|file)://))www\\.))" // 以http...或www开头
			+ ".*?" // 中间为任意内容，
			// + "(?=(&nbsp;|\\s|　|<br />|$|[<>]|,|;|\\.|，|；|。))"; // 结束条件
			+ "(?=(&nbsp;|\\s| |<br />|$|[,;<>{}“”？：；‘’！【】，。]))";
	Pattern pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);

	String username = "", password = "", connuid = "";
	String accessToken = "";
	String screenName = "";

	Queue<WeiboMsg> msgQueue = new LinkedList<WeiboMsg>();
	ResponseMsgThread responseThread = null;
	JSONObject uBasic = null;
	int ErrorCommentToMe = 0, ErrorMentionsToMe = 0, ErrorDirectMessage = 0;
	HashSet<String> rejectSource = new HashSet<String>();

	/*
	 * 以下为WeiboConnection 持有的Weibo对象
	 */
	Account account = new Account();
	Timeline timeline = new Timeline();
	PlaceTimeline placeTimeline = new PlaceTimeline();
	Comments comments = new Comments();
	DirectMessages directMessages = new DirectMessages();
	GeoPosition geoPosition = new GeoPosition();
	ShortUrl shortUrl = new ShortUrl();
	Users users = new Users();
	Paging page = new Paging();
	Friendships friendships = new Friendships();

	public void setAccessToken(String pToken) {
		this.accessToken = pToken;
		account.setToken(accessToken);
		timeline.setToken(accessToken);
		placeTimeline.setToken(accessToken);
		comments.setToken(accessToken);
		directMessages.setToken(accessToken);
		geoPosition.setToken(accessToken);
		shortUrl.setToken(accessToken);
		users.setToken(accessToken);
		friendships.setToken(accessToken);
	}

	/*
	 * 2.0修改了Oauth.authorize()接口，接收三个参数 在此版本内不许要此种连接方式，可暂时注掉。
	 * 此版本的函数体内，在Oauth.authorize方法中传了一个空的state值 如需code方式获取accessToken
	 * 需要获取一个state值。
	 */
	public boolean connect() throws WeiboException, IOException {
		int tryTimes = 0;
		accessToken = "";
		while (accessToken.length() < 20 && tryTimes < 5) {

			Oauth oauth = new Oauth();
			BareBonesBrowserLaunch.openURL(oauth.authorize("code", ""));
			System.out.println(oauth.authorize("code", ""));
			System.out.print("Hit enter when it's done.[Enter]:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String code = br.readLine();
			connlogger.info("code: " + code);
			try {
				accessToken = oauth.getAccessTokenByCode(code).getAccessToken();
			} catch (WeiboException e) {
				if (401 == e.getStatusCode()) {
					System.out.println("Unable to get the access token.");
					connlogger.info("Unable to get the access token.");
				} else {
					e.printStackTrace();
					connlogger.debug("Exception\tconnect\t" + e.getLocalizedMessage());
				}
			}
			if (accessToken != null) {
				if (accessToken.length() >= 20) {
					setAccessToken(accessToken);
					connuid = getUID();
					if (connuid.isEmpty()) {
						break;
					}
					screenName = this.getScreenName();
				} else {
					System.out.println("Will retry:" + tryTimes);
					connlogger.info("Will retry:" + tryTimes);
				}
			} else {
				accessToken = "";
				System.out.println("Will retry:" + tryTimes);
				connlogger.info("Will retry:" + tryTimes);
			}
			tryTimes++;
		}
		if (tryTimes >= 5 || connuid.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean connect(String pUsername, String pPassword) throws WeiboException, IOException {
		int tryTimes = 0;
		accessToken = "";
		this.username = pUsername;
		this.password = pPassword;
		while (accessToken.length() < 20 && tryTimes < 5) {
			Oauth oauth = new Oauth();
			try {
				accessToken = oauth.getAccessTokenByPassword(pUsername, pPassword).getAccessToken();
				System.out.println(accessToken);
			} catch (WeiboException e) {
				if (401 == e.getStatusCode()) {
					System.out.println("Unable to get the access token.");
					connlogger.info("Unable to get the access token.");
				} else {
					e.printStackTrace();
					connlogger.debug("Exception\tconnect\t" + e.getLocalizedMessage());
				}
			}
			if (accessToken != null) {
				if (accessToken.length() >= 20) {
					setAccessToken(accessToken);
					connuid = getUID();
					if (connuid.isEmpty()) {
						break;
					}
					screenName = this.getScreenName();
				} else {
					System.out.println("Will retry:" + tryTimes);
					connlogger.info("Will retry:" + tryTimes);
				}
			} else {
				accessToken = "";
				System.out.println("Will retry:" + tryTimes);
				connlogger.info("Will retry:" + tryTimes);
			}
			tryTimes++;
		}
		if (tryTimes >= 5 || connuid.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean connect(String pStrAccessToken) throws WeiboException, IOException {
		int tryTimes = 0;
		accessToken = "";
		while (accessToken.length() < 20 && tryTimes < 5) {
			if (pStrAccessToken != null) {
				if (pStrAccessToken.length() >= 20) {
					accessToken = pStrAccessToken;
					setAccessToken(accessToken);
					connuid = this.getUID();
					if (connuid.isEmpty()) {
						break;
					}
					screenName = this.getScreenName();
				} else {
					System.out.println("Will retry:" + tryTimes);
					connlogger.info("Will retry:" + tryTimes);
				}
			} else {
				accessToken = "";
				System.out.println("Will retry:" + tryTimes);
				connlogger.info("Will retry:" + tryTimes);
			}
			tryTimes++;
		}
		if (tryTimes >= 5 || connuid.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	public void setPaging(int pnPageSize) {
		page.setCount(pnPageSize);
	}

	/*
	 * 2.0已无getAccountBasic接口，如需要可增加，但需要获得高级权限 现已更改为 getUid()接口
	 */
	public String getUID() {
		String ruid = "";
		try {
			if (uBasic == null) {
				uBasic = this.account.getUid();
			}
			ruid = uBasic.getString("uid");
		} catch (Exception e) {
			System.out.println(accessToken);
			e.printStackTrace();
			uBasic = null;
			connlogger.debug("Exception\tgetUID\t" + e.getLocalizedMessage());
		}
		return ruid;
	}

	/*
	 * 2.0已无getAccountBasic接口，如需要可增加，但需要获得高级权限 现已更改为
	 * getUid()接口,只可获得ID，无法获得screenName 如特殊需要，可通过User获取
	 */
	public String getScreenName() {
		String screenName = "";
		try {
			User user = users.showUserById(connuid);
			screenName = user.getScreenName();
		} catch (Exception e) {
			connlogger.debug("Exception\tgetScreenName\t" + e.getLocalizedMessage());
		}
		return screenName;
	}

	// 根据获得UID的User模型
	public User getUserInfo(String uid) {
		User user = null;
		try {
			user = users.showUserById(uid);
		} catch (Exception e) {
			connlogger.debug("Exception\tgetUserInfo\t" + e.getLocalizedMessage());
		}
		return user;
	}

	public void GetBilateralTimeline() {
		try {
			StatusWapper status = this.timeline.getBilateralTimeline();
			for (Status s : status.getStatuses()) {
				connlogger.info(s.toString());
			}
			System.out.println(status.getNextCursor());
			System.out.println(status.getPreviousCursor());
			System.out.println(status.getTotalNumber());
			System.out.println(status.getHasvisible());
		} catch (WeiboException e) {
			connlogger.debug("Exception\tGetBilateralTimeline\t" + e.getLocalizedMessage());
		}
	}

	/*
	 * 2.0版本没有PlaceTimeLine这个类，其基本内容可用weibo4j.Place代替， 如需使用原来的接口，可做对应改造。
	 */
	public void GetUserPlaceTimeline(String uid) {
		try {
			StatusWapper status = this.placeTimeline.getUserPalaceTimelineByUid(uid);
			for (Status s : status.getStatuses()) {
				connlogger.info(s.toString());
				System.out.println(s.toString());
			}
			System.out.println(status.getNextCursor());
			System.out.println(status.getPreviousCursor());
			System.out.println(status.getTotalNumber());
			System.out.println(status.getHasvisible());
		} catch (WeiboException e) {
			connlogger.debug("Exception\tGetUserPlaceTimeline\t" + e.getLocalizedMessage());
		}
	}

	public void getFriendsTimeline() {
		try {
			StatusWapper status = this.timeline.getFriendsTimeline();
			for (Status s : status.getStatuses()) {
				connlogger.info(s.toString());
			}
			System.out.println(status.getNextCursor());
			System.out.println(status.getPreviousCursor());
			System.out.println(status.getTotalNumber());
			System.out.println(status.getHasvisible());
		} catch (WeiboException e) {
			connlogger.debug("Exception\tgetFriendsTimeline\t" + e.getLocalizedMessage());
		}

	}

	public void getHomeTimeline() {
		try {
			StatusWapper status = this.timeline.getHomeTimeline();
			for (Status s : status.getStatuses()) {
				connlogger.info(s.toString());
			}
			System.out.println(status.getNextCursor());
			System.out.println(status.getPreviousCursor());
			System.out.println(status.getTotalNumber());
			System.out.println(status.getHasvisible());
		} catch (WeiboException e) {
			connlogger.debug("Exception\tgetFriendsTimeline\t" + e.getLocalizedMessage());
		}
	}

	/*
	 * 2.0接口去掉了weibo4j.model.Comment 中的GEO信息
	 */
	public long getCommentToMe(ArrayList<WeiboMsg> msgs, Date lastTime, Long lastID, MsgQueue msgQueue) {
		try {
			CommentWapper comment = null;

			if (lastID > 0) {
				comment = this.comments.getCommentToMe(lastID);
			} else {
				comment = this.comments.getCommentToMe();
			}
			long timeLimen = this.getTimeLimen();

			for (Comment c : comment.getComments()) {
				try {
					if (c.getId() > lastID && c.getCreatedAt().getTime() < timeLimen) {
						lastID = c.getId();
					}

					if (c.getCreatedAt().getTime() > lastTime.getTime()) {
						if (c.getUser().getId().equals(connuid)) {
							connlogger.info("getCommentToMe\tMsg_self\t" + c.getUser().getId());
							c = null;
							continue;
						}
						if (!msgQueue.isNewOne(c.getMid())) {
							connlogger.info("getCommentToMe\tMsg_old\t" + c.getMid() + "\t" + msgQueue.msgCnt());
							c = null;
							continue;
						}

						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(c.getUser());
						msg.setCreatedTime(c.getCreatedAt());
						msg.setMsgInfo("Type", "comment");
						msg.setMsgInfo("Mid", c.getStatus().getMid()); // 当前评论的所在博文
						msg.setMsgInfo("Cid", c.getMid()); // 当前评论的id
						msg.setMsgInfo("Content", extractContent(c.getText(), msg.getMsgInfo("Type")));
						msg.setMsgInfo("FullContent", c.getText());

						if (c.getLongitude() > 0 && c.getLatitude() > 0) {
							msg.setGeo(c.getLongitude(), c.getLatitude());
							String address = this.getAddressbyGEO(c.getLongitude(), c.getLatitude());
							msg.setGeoPosition(address);
						}

						msgs.add(msg);
					}
					connlogger.info("getCommentToMe\tMsg\t" + c.toString());
				} catch (Exception e) {
					connlogger.info("Exception\tgetCommentToMe\t" + e.getLocalizedMessage());
				}
			}
			connlogger.info("getCommentToMe\tMsg_count\t" + msgs.size());
			ErrorCommentToMe = 0;
		} catch (WeiboException e) {
			ErrorCommentToMe++;
			connlogger.info("Exception\tgetCommentToMe\t" + e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorCommentToMe++;
			connlogger.info("Exception\tgetCommentToMe\t" + e.getLocalizedMessage());
		}
		return lastID;
	}

	public long getCommentMentionsToMe(ArrayList<WeiboMsg> msgs, Date lastTime, Long lastID, MsgQueue msgQueue) {
		try {
			CommentWapper comment = null;

			if (lastID > 0) {
				comment = this.comments.getCommentMentions(lastID);
			} else {
				comment = this.comments.getCommentMentions();
			}

			// 默认取第1页，前10个
			// comment = this.comments.getCommentMentions(page,0,0);

			long timeLimen = this.getTimeLimen();
			for (Comment c : comment.getComments()) {
				try {
					if (c.getId() > lastID && c.getCreatedAt().getTime() < timeLimen) {
						lastID = c.getId();
					}

					if (c.getCreatedAt().getTime() > lastTime.getTime()) {
						if (c.getUser().getId().equals(connuid)) {
							connlogger.info("getCommentMentionsToMe\tMsg_self\t" + c.getUser().getId());
							c = null;
							continue;
						}
						if (!msgQueue.isNewOne(c.getMid())) {
							connlogger.info("getCommentMentionsToMe\tMsg_old\t" + c.getMid() + "\t" + msgQueue.msgCnt());
							c = null;
							continue;
						}

						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(c.getUser());
						msg.setCreatedTime(c.getCreatedAt());
						msg.setMsgInfo("Type", "comment");
						msg.setMsgInfo("Mid", c.getStatus().getMid()); // 当前评论的所在博文
						msg.setMsgInfo("Cid", c.getMid()); // 当前评论的id
						msg.setMsgInfo("Content", extractContent(c.getText(), msg.getMsgInfo("Type")));
						msg.setMsgInfo("FullContent", c.getText());

						if (c.getLongitude() > 0 && c.getLatitude() > 0) {
							msg.setGeo(c.getLongitude(), c.getLatitude());
							String address = this.getAddressbyGEO(c.getLongitude(), c.getLatitude());
							msg.setGeoPosition(address);
						}

						msgs.add(msg);
					}
					connlogger.info("getCommentMentionsToMe\tMsg\t" + c.toString());
				} catch (Exception e) {
					connlogger.info("Exception\tgetCommentMentionsToMe\t" + e.getLocalizedMessage());
				}
			}
			ErrorCommentToMe = 0;
			connlogger.info("getCommentMentionsToMe\tMsg_count\t" + msgs.size());
		} catch (WeiboException e) {
			ErrorCommentToMe++;
			connlogger.debug("Exception\tgetCommentMentionsToMe\t" + e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorCommentToMe++;
			connlogger.debug("Exception\tgetCommentMentionsToMe\t" + e.getLocalizedMessage());
		}
		return lastID;
	}

	public boolean postComment(String pId, String pComments) {
		String[] msgs = pComments.split("\\{MSG_END\\}");
		boolean succ = false;
		for (String msg : msgs) {
			succ = postComment(pId, msg, 0);
		}
		return succ;
	}

	public boolean postComment(String pId, String pComments, int reTrys) {
		String id = pId;
		String comments = pComments;
		boolean succ = false;

		if (comments.length() > 0 && id.length() > 0) {
			try {
				ArrayList<String> sumComments = SplitbyCount(comments, 140);
				for (String subComment : sumComments) {
					Comment comment = this.comments.createComment(subComment, id);
					connlogger.info(comment.toString());
				}
				succ = true;
			} catch (WeiboException e) {
				if (e.getErrorCode() == 20019) {
					connlogger.debug("ReTry[" + reTrys + "]:" + e.getLocalizedMessage());
					if (reTrys < 3) {
						try {
							Thread.sleep(500);
						} catch (Exception ee) {
						}
						succ = postComment(pId, pComments + ".", reTrys + 1);
					} else {
						succ = false;
					}
				} else {
					e.printStackTrace();
					connlogger.debug("Exception\tpostComment\t" + e.getLocalizedMessage());
				}
			}
		}
		return succ;
	}

	public long getMentionsToMe(ArrayList<WeiboMsg> msgs, Date lastTime, Long lastID, MsgQueue msgQueue) {
		try {
			StatusWapper status = null;

			if (lastID > 0) {
				status = timeline.getMentions(lastID);
			} else {
				status = timeline.getMentions();
			}
			long timeLimen = this.getTimeLimen();

			for (Status s : status.getStatuses()) {
				try {
					if (s.getIdstr() > lastID && s.getCreatedAt().getTime() < timeLimen) {
						lastID = s.getIdstr();
					}

					if (s.getCreatedAt().getTime() > lastTime.getTime()) {
						if (s.getUser().getId().equals(connuid)) {
							connlogger.info("getMentionsToMe\tMsg_self\t" + s.getUser().getId());
							continue;
						}
						if (!msgQueue.isNewOne(s.getMid())) {
							connlogger.info("getMentionsToMe\tMsg_old\t" + s.getMid() + "\t" + msgQueue.msgCnt());
							continue;
						}

						if (rejectSource.contains(s.getSource().getName())) {
							connlogger.info("getMentionsToMe\tMsg_refused_source\t" + s.getSource().getName());
							continue; // 跳过被拒绝来源的博文
						}

						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(s.getUser());
						msg.setCreatedTime(s.getCreatedAt());
						msg.setMsgInfo("Type", "mention");
						msg.setMsgInfo("Mid", s.getMid());
						msg.setMsgInfo("Cid", s.getId());
						msg.setMsgInfo("Content", extractContent(s.getText(), msg.getMsgInfo("Type")));
						msg.setMsgInfo("FullContent", s.getText());

						if (s.getLongitude() > 0 && s.getLatitude() > 0) {
							msg.setGeo(s.getLongitude(), s.getLatitude());
							String address = this.getAddressbyGEO(s.getLongitude(), s.getLatitude());
							msg.setGeoPosition(address);
						}

						msgs.add(msg);
						connlogger.info(connuid + "\tgetMentionsToMe\tMsg\t" + s.toString());
					}

				} catch (Exception e) {
					connlogger.info("Exception\tgetMentionsToMe\t" + connuid + "\t" + e.getLocalizedMessage());
				}
			}
			ErrorMentionsToMe = 0;
			connlogger.info(connuid + "\tgetMentionsToMe\tMsg_count\t" + msgs.size());
		} catch (WeiboException e) {
			ErrorMentionsToMe++;
			// e.printStackTrace();
			connlogger.debug("Exception\tgetMentionsToMe\terr_wb\t" + e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorMentionsToMe++;
			// e.printStackTrace();
			connlogger.debug("Exception\tgetMentionsToMe\terr\t" + e.getLocalizedMessage());
		}
		return lastID;
	}

	public Status postWeibo(String msg) {
		String statuses = msg;
		try {
			Status status = this.timeline.UpdateStatus(statuses);
			connlogger.info(status.toString());
			return status;
		} catch (WeiboException e) {
			connlogger.debug("Exception\tpostWeibo\t" + e.getLocalizedMessage());
			return null;
		}
	}

	public HashSet<String> getNewFansToMe(ArrayList<WeiboMsg> msgs, HashSet<String> hasFindFansSet) {
		String[] followersIds = null;
		int endIdx = 0;
		try {
			if (hasFindFansSet.size() <= 0) { // 程序第一次启动获取5个粉丝uid
				followersIds = this.friendships.getFollowersIdsById(this.connuid, 20);
				for (int i = 0; i < 5; i++) {
					hasFindFansSet.add(followersIds[i]);
				}
				connlogger.info("getNewFansToMe\tfirst set\t" + hasFindFansSet.toString());
			} else {
				int unreadFollowers = 0;
				JSONObject jsonUnread = this.timeline.getUnreadCount(this.connuid);
				unreadFollowers = jsonUnread.getInt("follower");// 获取未读粉丝数
				if (unreadFollowers == 0) {
					return hasFindFansSet; // 未读粉丝数=0，返回初始粉丝列表
				} else {
					followersIds = this.friendships.getFollowersIdsById(this.connuid, unreadFollowers); // 获取粉丝列表
				}

				connlogger.info("getNewFansToMe\tunreadFollowers=" + unreadFollowers);

				for (int i = 0; i < unreadFollowers; i++) {
					String newFansUid = followersIds[i];
					if (!hasFindFansSet.contains(newFansUid)) { // 前unreadFollowers个粉丝如果不在列表中认为是新粉丝
						User Sender = getUserInfo(newFansUid);
						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(Sender);
						msg.setCreatedTime(new Date());
						msg.setMsgInfo("Type", "newFans");
						msg.setMsgInfo("Content", "");
						msg.setMsgInfo("IMGID", "0");
						connlogger.info("getNewFansToMe\tnewfans\t" + msg.toString());
						msgs.add(msg);
					} else {
						endIdx = i; // 发现第i个在粉丝列表的uid，记录i，循环退出
						break;
					}
				}
				if (endIdx < 5) {
					endIdx = 5;
				}
				hasFindFansSet.clear();
				for (int i = 0; i < endIdx; i++) {
					hasFindFansSet.add(followersIds[i]);
				}
				connlogger.info("getNewFansToMe\trefresh set\tendIdx=" + endIdx + "\t" + hasFindFansSet.toString());
			}
		} catch (Exception e) {
			connlogger.debug("Exception\tgetNewFansToMe\t" + e.getLocalizedMessage());
			hasFindFansSet.clear();
			for (int i = 0; i < endIdx; i++) {
				hasFindFansSet.add(followersIds[i]);
			}
			connlogger.info("Exception\tgetNewFansToMe\trefresh set\tendIdx=" + endIdx + "\t" + hasFindFansSet.toString());

		}

		return hasFindFansSet;
	}

	/*
	 * 取消息箱中内容
	 */
	public long getMessageBoxMessages(ArrayList<WeiboMsg> msgs, Date lastTime, Long lastID, MsgQueue msgQueue) {
		try {
			DirectMessageWapper dms = null;

			if (lastID > 0) {
				dms = directMessages.getNewMessageBoxMessages(lastID);
			} else {
				dms = directMessages.getNewMessageBoxMessages();
			}

			long timeLimen = this.getTimeLimen();

			for (DirectMessage m : dms.getDirectMessages()) {
				try {
					if (m.getId() > lastID && m.getCreatedAt().getTime() < timeLimen) {
						lastID = m.getId();
					}

					if (m.getCreatedAt().getTime() > lastTime.getTime()) {
						if (m.getSender().getId().equals(connuid)) {
							connlogger.info("getMessageBoxMessages\tMsg_self\t" + m.getSender().getId());
							m = null;
							continue;
						}
						if (!msgQueue.isNewOne(m.getMid())) {
							connlogger.info("getMessageBoxMessages\tMsg_old\t" + m.getMid() + "\t" + msgQueue.msgCnt());
							m = null;
							continue;
						}

						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(m.getSender());
						msg.setCreatedTime(m.getCreatedAt());
						msg.setMsgInfo("Type", "message");
						msg.setMsgInfo("Mid", m.getMid());
						msg.setMsgInfo("Cid", String.valueOf(m.getId()));
						msg.setMsgInfo("Content", m.getText());

						if (m.getLongitude() > 0 && m.getLatitude() > 0) {
							msg.setGeo(m.getLongitude(), m.getLatitude());
							String address = this.getAddressbyGEO(m.getLongitude(), m.getLatitude());
							msg.setGeoPosition(address);
						}

						msgs.add(msg);
					}
					connlogger.info("getMessageBoxMessages\tMsg\t" + m.toString());
				} catch (Exception e) {
					connlogger.info("Exception\tgetMessageBoxMessages\t" + e.getLocalizedMessage());
				}
			}
			ErrorDirectMessage = 0;
			connlogger.info("getMessageBoxMessages\tMsg_count\t" + msgs.size());
		} catch (WeiboException e) {
			ErrorDirectMessage++;
			connlogger.debug("Exception\tgetMessageBoxMessages\t" + e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorDirectMessage++;
			connlogger.debug("Exception\tgetMessageBoxMessages\t" + e.getLocalizedMessage());
		}
		return lastID;
	}

	/*
	 * 不知道是不是需要高级权限的缘故，2.0接口已经没有weibo4j.DirectMessages 这个类了，也没想明白如何发起私信服务
	 * 现，需要，可将1.0的接口移植过来，并申请权限
	 * 
	 * 同时 weibo4j.model.DirectMessage，weibo4j.model.DirectMessageWapper
	 */
	public long getDirectMessage(ArrayList<WeiboMsg> msgs, Date lastTime, Long lastID, MsgQueue msgQueue) {
		try {
			DirectMessageWapper dms = null;

			if (lastID > 0) {
				dms = directMessages.getNewDirectMessages(lastID);
			} else {
				dms = directMessages.getNewDirectMessages();
			}

			long timeLimen = this.getTimeLimen();

			for (DirectMessage m : dms.getDirectMessages()) {
				try {
					if (m.getId() > lastID && m.getCreatedAt().getTime() < timeLimen) {
						lastID = m.getId();
					}

					if (m.getCreatedAt().getTime() > lastTime.getTime()) {
						if (m.getSender().getId().equals(connuid)) {
							connlogger.info("getDirectMessage\tMsg_self\t" + m.getSender().getId());
							m = null;
							continue;
						}
						if (!msgQueue.isNewOne(m.getMid())) {
							connlogger.info("getDirectMessage\tMsg_old\t" + m.getMid() + "\t" + msgQueue.msgCnt());
							m = null;
							continue;
						}

						WeiboMsg msg = new WeiboMsg();
						msg.setUserInfo(m.getSender());
						msg.setCreatedTime(m.getCreatedAt());
						msg.setMsgInfo("Type", "message");
						msg.setMsgInfo("Mid", m.getMid());
						msg.setMsgInfo("Cid", String.valueOf(m.getId()));
						msg.setMsgInfo("Content", m.getText());

						if (m.getLongitude() > 0 && m.getLatitude() > 0) {
							msg.setGeo(m.getLongitude(), m.getLatitude());
							String address = this.getAddressbyGEO(m.getLongitude(), m.getLatitude());
							msg.setGeoPosition(address);
						}

						msgs.add(msg);
					}
					connlogger.info("getDirectMessage\tMsg\t" + m.toString());
				} catch (Exception e) {
					connlogger.info("Exception\tgetDirectMessage\t" + e.getLocalizedMessage());
				}
			}
			ErrorDirectMessage = 0;
			connlogger.info("getDirectMessage\tMsg_count\t" + msgs.size());
		} catch (WeiboException e) {
			ErrorDirectMessage++;
			connlogger.debug("Exception\tgetDirectMessage\t" + e.getLocalizedMessage());
		} catch (Exception e) {
			ErrorDirectMessage++;
			connlogger.debug("Exception\tgetDirectMessage\t" + e.getLocalizedMessage());
		}
		return lastID;
	}

	public DirectMessage postDirectMessage(String uid, String text, String imgid) {
		String[] msgs = text.split("\\{MSG_END\\}");
		DirectMessage resultMessage = null;
		for (int i = 0; i < msgs.length; i++) {
			if (i == (msgs.length - 1))
				resultMessage = postDirectMessage(uid, msgs[i], imgid, 0); // 最后一条时发送图片
			else
				resultMessage = postDirectMessage(uid, msgs[i], "0", 0);
		}
		return resultMessage;
	}

	public DirectMessage postDirectMessage(String uid, String text, String imgid, int reTrys) {
		try {
			int length = text.length();
			if (length > 290) {
				text = text.substring(0, 290);
			}
			String[] imgids = imgid.split(",");
			DirectMessage msg = null;
			for (int i = 0; i < imgids.length; i++) {
				if (i == 0)
					msg = this.timeline.sendDirectMessageInter(uid, text, imgids[i]); // 只在第一条消息中发送消息内容
				else
					msg = this.timeline.sendDirectMessageInter(uid, "图片" + (i + 1), imgids[i]);
				connlogger.info(msg.toString());
			}
			return msg;
		} catch (WeiboException e) {
			if (e.getErrorCode() == 20019) {
				connlogger.debug("ReTry[" + reTrys + "]:" + e.getLocalizedMessage());
				if (reTrys < 3) {
					try {
						Thread.sleep(500);
					} catch (Exception ee) {
					}
					return postDirectMessage(uid, text + ".", imgid, reTrys + 1);
				} else {
					return null;
				}
			} else {
				connlogger.debug("Exception\tpostDirectMessage\t" + e.getLocalizedMessage());
				return null;
			}
		}
	}

	private String getRuleAddress(String provinceName, String cityName, String address) {
		String country = "中国";
		String[] TheCitys = { "北京", "天津", "上海", "重庆" };
		boolean isTheCity = false;

		String rAddress = "";
		String dAddress = address.replace(provinceName + cityName, "");

		int idx = dAddress.indexOf("区");
		if (idx < 1) {
			idx = dAddress.indexOf("市");
		}
		if (idx >= 1) {
			rAddress = dAddress.substring(0, idx + 1) + "." + dAddress.substring(idx + 1);
		} else {
			rAddress = dAddress;
		}
		for (String city : TheCitys) {
			if (provinceName.indexOf(city) >= 0) {
				isTheCity = true;
				break;
			}
		}
		if (isTheCity) {
			rAddress = country + "." + cityName + "." + rAddress;
		} else {
			rAddress = country + "." + provinceName + "." + cityName + "." + rAddress;
		}

		return rAddress;
	}

	/*
	 * 2.0接口没有GeoPosition 以及Geo两个类
	 */
	public String getAddressbyGEO(double pLong, double pLat) {
		String address = "";
		try {
			ArrayList<Geo> geos = this.geoPosition.getAddressByGeo(pLong, pLat);

			for (int i = 0; i < geos.size(); i++) {
				if (address.length() > 1) {
					address = address + ";";
				}
				address = address + getRuleAddress(geos.get(i).getProvinceName(), geos.get(i).getCityName(), geos.get(i).getAddress());
			}
		} catch (Exception e) {
			connlogger.debug("Exception\tgetAddressbyGEO\t" + e.getLocalizedMessage());
		}

		return address;
	}

	public ArrayList<Double> getGEObyAddress(String address) {
		ArrayList<Double> addresss = new ArrayList<Double>();
		try {
			ArrayList<Geo> geos = this.geoPosition.getGeoByAddress(address);

			for (int i = 0; i < geos.size(); i++) {
				addresss.add(geos.get(i).getLongitude());
				addresss.add(geos.get(i).getLatitude());
			}
		} catch (Exception e) {
			connlogger.debug("Exception\tgetGEObyAddress\t" + e.getLocalizedMessage());
		}

		return addresss;
	}

	public boolean checkConnect() {
		if (ErrorCommentToMe > 10 || ErrorMentionsToMe > 10 || ErrorDirectMessage > 10) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * 为2.0接口而做的修改，主要修改了weibo4j.ShortUrl批量短网址实现接口 返回HashMap
	 */

	public HashMap<String, String> longUrl2short(ArrayList<String> longurls) {
		try {
			return this.shortUrl.getShortUrlMap(longurls);
		} catch (Exception e) {
			connlogger.debug("Exception\tlongUrl2short" + e.getLocalizedMessage());
			return null;
		}
	}

	public String replaceLongUrl(String str) {
		Matcher matcher = pattern.matcher(str);

		ArrayList<String> longurls = new ArrayList<String>();
		while (matcher.find()) {
			longurls.add(matcher.group(0));
		}
		if (longurls.size() != 0) {
			HashMap<String, String> urlmap = this.longUrl2short(longurls);
			if (urlmap != null) {
				for (String lUrl : urlmap.keySet()) {
					String sUrl = urlmap.get(lUrl);
					if (sUrl != null) {
						str = str.replace(lUrl, sUrl);
					}
				}
			}
		}
		return str;
	}

	public int putResponseMsg(WeiboMsg pMsg) {
		this.msgQueue.add(pMsg);
		return this.msgQueue.size();
	}

	public void beginResponse() {
		if (responseThread == null) {
			responseThread = new ResponseMsgThread(this.msgQueue, this);
			responseThread.setName(connuid + "-ResponseThread");
			responseThread.start();
		} else {
			if (!responseThread.isAlive()) {
				responseThread.start();
			}
		}
	}

	public void stopResponse() {
		responseThread.stop = true;
		System.out.println("Stop Thread " + connuid + "-Conn.Response");
	}

	class ResponseMsgThread extends Thread {
		Queue<WeiboMsg> msgSource = null;
		WeiboConnection con = null;
		public boolean stop = false;

		ResponseMsgThread(Queue<WeiboMsg> pMsgSource, WeiboConnection pCon) {
			this.msgSource = pMsgSource;
			this.con = pCon;
		}

		public void run() {
			stop = false;

			while (!stop) {
				try {
					WeiboMsg msg = this.msgSource.poll();
					if (msg != null) {
						String rsen = msg.getMsgInfo("Response");
						if (rsen == null) {
							rsen = ":)";
						} else {
							rsen = this.con.replaceLongUrl(rsen);
						}
						if (msg.getMsgInfo("Type").equals("comment")) { // 评论中提及，也是comment
							rsen = "回复@" + msg.getUserInfo().getName() + ":" + rsen;
							rsen = rsen.replaceAll("\\{MSG_END\\}", "\\{MSG_END\\}回复@" + msg.getUserInfo().getName() + ":");
						}

						if (msg.getMsgInfo("Type").equals("comment") || msg.getMsgInfo("Type").equals("mention")) {
							connlogger.info("ReComment\tOUT\t" + msg.getUserInfo().getId() + "\t" + msg.getUserInfo().getName() + "\t"
									+ msg.getMsgInfo("Mid") + "\t" + rsen + "\t" + msg.getCreatedTime());
							boolean succ = con.postComment(msg.getMsgInfo("Mid"), rsen);
							if (!succ) { // post failed
								String retry = msg.getMsgInfo("retry");
								if (retry.equals("")) {
									retry = "0";
								}

								Integer retryi = Integer.valueOf(retry);
								if (retryi < 5) { // add into queue
									msg.setMsgInfo("retry", String.valueOf(retryi + 1));
									this.msgSource.add(msg);
								} else { // drop
									connlogger.info("ReComment\tDROP\t" + msg.getUserInfo().getId() + "\t" + msg.getUserInfo().getName() + "\t"
											+ msg.getMsgInfo("Mid") + "\t" + rsen + "\t" + msg.getCreatedTime());
								}
							}
						} else if (msg.getMsgInfo("Type").equals("message")) {
							connlogger.info("ReMessage\tOUT\t" + msg.getUserInfo().getId() + "\t" + msg.getUserInfo().getName() + "\t"
									+ msg.getMsgInfo("Mid") + "\t" + rsen + "\t" + msg.getCreatedTime());
							DirectMessage dmsg = con.postDirectMessage(msg.getUserInfo().getId(), rsen, msg.getMsgInfo("IMGID"));
							if (dmsg == null) { // post failed
								String retry = msg.getMsgInfo("retry");
								if (retry.equals("")) {
									retry = "0";
								}

								Integer retryi = Integer.valueOf(retry);
								if (retryi < 5) { // add into queue
									msg.setMsgInfo("retry", String.valueOf(retryi + 1));
									this.msgSource.add(msg);
								} else { // drop
									connlogger.info("ReMessage\tDROP\t" + msg.getUserInfo().getId() + "\t" + msg.getUserInfo().getName() + "\t"
											+ msg.getMsgInfo("Mid") + "\t" + rsen + "\t" + msg.getCreatedTime());
								}
							}
						}

					} else {
						Thread.sleep(500);
					}
				} catch (Exception e) {
					connlogger.info("Exception\tResponseMsgThread\t" + e.getLocalizedMessage());
				}
			}

		}
	}

	public String toString() {
		String info = "";

		info = "username=" + this.username + ";password=" + this.password + ";token=" + this.accessToken + ";reponsen=" + this.msgQueue.toString();

		return info;
	}

	public boolean setRejectSource(String rejectSources) {
		this.rejectSource.clear();
		if (rejectSources != null) {
			String[] sources = rejectSources.trim().split(";");
			for (int i = 0; i < sources.length; i++) {
				if (sources[i].length() > 0) {
					rejectSource.add(sources[i]);
				}
			}
		}
		return true;
	}

	private String extractContent(String fullContent, String msgType) {

		String content = "";
		String screenName = this.getScreenName().trim();
		content = fullContent.replaceFirst("@" + screenName, "").trim();
		content = content.replaceAll(" ", "");
		content = content.replaceAll("http://", "http:##").trim();
		int idx = content.indexOf("//");
		if (idx >= 0) {
			content = content.substring(0, idx);
		}
		content = content.replaceAll("http:##", "http://").trim();

		if (!msgType.equals("message")) {
			if (content.indexOf("回复") != -1) {
				int pivot = content.indexOf(":") + 1;
				content = content.substring(pivot, content.length());
			}
		}

		return content;
	}

	public String getAccsessToken() {
		return accessToken;
	}

	public static void main(String[] args) {
		WeiboConnection con = new WeiboConnection();
		try {

			// con.connect("2.00PtG_ZB0JcFYM25ec65609c0jUWis"); mycFelix
			// con.connect("2.00lyPc4D0JcFYM1477953d50oVfq5B"); //客微助理
			// System.out.println(con.connuid);

			String token = "2.00Xg9TpDrmAWzBb0ca66ad1fKGweCE";

			Oauth oauth = new Oauth();
			oauth.setToken(token);
			con.connect(token); // 客微助理
			JSONObject jsonObject = oauth.getTokenedUserInfo(token);
			System.out.println(jsonObject.toString(5));

			// Timeline timeline = new Timeline();
			// timeline.setToken("2.00lyPc4D0JcFYMda52609f3dwXeUOE");
			// jsonObject = timeline.getUnreadCount("1437859313");
			// System.out.println(jsonObject.toString(5));
			//
			// Friendships friendships = new Friendships();
			// friendships.setToken("2.00lyPc4D0JcFYMda52609f3dwXeUOE");
			// String[] aaa= friendships.getFollowersIdsById("1437859313");
			// for(String str:aaa){
			// System.out.println(str);
			// }

			// ArrayList<String> longurls = new ArrayList<String>();
			// longurls.add("http://www.sina.com.cn");
			// longurls.add("http://www.google.com.hk");
			// longurls.add("http://sae.sina.com.cn");
			//
			// HashMap<String, String> ret = con.longUrl2short(longurls);
			// Iterator iterator = ret.keySet().iterator();
			// while(iterator.hasNext()){
			// String key = (String)iterator.next();
			// System.out.println(key+"\t"+ret.get(key));
			// }

			// ArrayList<WeiboMsg> msgs = new ArrayList<WeiboMsg>();
			// HashSet<String> hasFindFansSet = new HashSet<String>();
			// long n=0;
			// Date d = new Date();
			//
			// Long CommentToMeID = 0L, DirectMsgID = 0L, MetionsToMeID = 0L,
			// CommentMentionID = 0L;
			// MsgQueue ctmQueue = new MsgQueue(5000);
			// MsgQueue dmQueue = new MsgQueue(5000);
			// MsgQueue mtmQueue = new MsgQueue(5000);
			// MsgQueue cmQueue = new MsgQueue(5000);
			// while (true){
			// msgs.clear();
			// n=0;
			//
			// CommentToMeID = con.getCommentToMe(msgs, d,
			// CommentToMeID, ctmQueue);
			// DirectMsgID = con.getDirectMessage(msgs, d, DirectMsgID,
			// dmQueue);
			// MetionsToMeID = con.getMentionsToMe(msgs, d,
			// MetionsToMeID, mtmQueue);
			// CommentMentionID = con.getCommentMentionsToMe(msgs, d,
			// CommentMentionID, cmQueue);
			// hasFindFansSet = con.getNewFansToMe(msgs, hasFindFansSet);
			//
			// if(msgs.size()>0){
			// String re = "hello,这是自动回复", rsen="";
			// for(WeiboMsg msg:msgs){
			// if(msg.getCreatedTime().getTime()>d.getTime()){
			// d = msg.getCreatedTime();
			// }
			// System.out.println(msg.getUserInfo().getName()+":"+msg.getMsgInfo("Mid")+":"+msg.getMsgInfo("Cid")+":"+msg.getMsgInfo("Content"));
			// connlogger.info(msg.getMsgInfo("Type")+"\tIN\t"+msg.getUserInfo().getId()+"\t"+msg.getUserInfo().getName()+"\t"+msg.getMsgInfo("Mid")+"\t"+msg.getMsgInfo("Content")+"\t"+msg.getCreatedTime());
			//
			// if(msg.getMsgInfo("Type").equals("comment") ||
			// msg.getMsgInfo("Type").equals("mention")){
			// rsen="回复@"+msg.getUserInfo().getScreenName()+":"+re+"("+msg.getMsgInfo("Type")+" by : "+
			// msg.getMsgInfo("Content").replace("@", "") +")";
			// con.postComment(msg.getMsgInfo("Mid"), rsen);
			// }else if(msg.getMsgInfo("Type").equals("message")){
			// rsen=re+"("+msg.getMsgInfo("Type")+":"+msg.getMsgInfo("Content")+")";
			// con.postDirectMessage(msg.getUserInfo().getId(),
			// rsen,msg.getMsgInfo("IMGID"));
			// }
			//
			// System.out.println("To "+msg.getMsgInfo("Mid")+":"+rsen);
			// connlogger.info("To "+msg.getUserInfo().getName()+":"+rsen);
			// }
			// }
			//
			// Thread.sleep(1000);
			// }
			//
			//
			//
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private long getTimeLimen() {
		Date msgnow = new Date();
		long limen = msgnow.getTime();
		limen = limen - 1000 * 60 * 2;// 2分钟
		return limen;
	}

	public boolean isDbcCase(char c) {
		if (c >= 32 && c <= 127) {// 基本拉丁字母(即键盘上可见的，空格、数字、字母、符号)
			return true;
		} else if (c >= 65377 && c <= 65439) {// 日文半角片假名和符号
			return true;
		}
		return false;
	}

	public ArrayList<String> SplitbyCount(String pStr, double dLimit) {
		ArrayList<String> outList = new ArrayList<String>();
		double dCount = 0, dLen = 0;
		String ourString = "";
		for (int i = 0; i < pStr.length(); i++) {
			char c = pStr.charAt(i);
			if (isDbcCase(c)) { // 半角
				dLen = 0.5;
			} else { // 全角
				dLen = 1.0;
			}
			if ((dCount + dLen) > dLimit) {
				outList.add(ourString + "");
				ourString = "" + c;
				dCount = dLen;
			} else {
				ourString += c;
				dCount += dLen;
			}
		}
		if (!ourString.isEmpty()) {
			outList.add(ourString + "");
		}

		return outList;
	}

}
