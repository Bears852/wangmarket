package com.xnx3.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.xnx3.ConfigManagerUtil;
import com.xnx3.j2ee.Global;
import com.xnx3.net.AliyunLogUtil;
import com.xnx3.net.AliyunSMSUtil;
import com.xnx3.admin.entity.Site;

/**
 * 全局
 * @author 管雷鸣
 */
public class G {
	private static Logger logger = Logger.getLogger(G.class); 
	
	//云端域名。如，云端模版列表，则为 cloudDomain+"cloudTemplateList.do"
	public static String cloudDomain = "http://wang.market/";
	
	public static int agencyAddSubAgency_siteSize = 20;	//代理开通下级代理，消耗20站币
	
	public static final boolean SITE_MYSELF_USED = true;	//网站是否是wang.market自己使用，若是，为true，若不是，是将其部署到其他的服务器上，则为false
	public static final boolean SITE_DEPLOYMODE_SHARE = true;	//当前的部署模式，是否是共享一台服务器，即网站建设项目跟域名转发绑定都在一个项目里，都共用一个服务器。若是，则为true
	
	//站点信息缓存
	public static final String VERSION;	//当前的版本号。v1.x为通用模版时代； v2.x为cms时代, 如 v2.25.1.20170908_RC
	public static final String CACHE_FILE = "cache/data/";
	public static final String DEFAULT_SITE_COLUMN_ICON_URL = "res/glyph-icons/world.png";
	public static final String RES_CDN_DOMAIN = "http://res.weiunity.com/";	//CDN缓存的资源文件，包括框架的js、css文件、模版style.css文件等。建议不要改动，不然很多地方可能都无法使用。
	
	public static final int TEMPLATE_PC_DEFAULT = 3;	//PC端的默认模版编号是3
	public static final int TEMPLATE_WAP_DEFAULT = 1;	//手机端的默认模版编号是1
	
	public static final int PAGE_WAP_NUM = 12;	//手机版本的新闻、图文列表，每页显示的条数
	
	public static final int SITECOLUMN_ICON_MAXWIDTH = 100;	//siteColumn的icon图标上传后缩放的最大宽度
	public static final int CAROUSEL_MAXWIDTH = 2600;			//轮播图的最大宽度
	public static final int NEWS_TITLEPIC_MAXWIDTH = 1000;		//新闻图片的titlepic的最大宽度
	
	/* PC端 */
	public static final int SITECOLUMN_ICON_MAXWIDTH_PC = 600;	//siteColumn的icon图标上传后缩放的最大宽度
	public static final int CAROUSEL_MAXWIDTH_PC = 4000;			//轮播图的最大宽度
//	public static final int NEWS_TITLEPIC_MAXWIDTH_PC = 1200;		//新闻图片的titlepic的最大宽度
	
	public static final String DEFAULT_PC_ABOUT_US_TITLEPIC = RES_CDN_DOMAIN+"default_image/aboutUs.jpg";	//默认的关于我们的图
	
	public static String forbidDomain = ",www,wap,m,3g,4g,5g,mail,domain,idc,service,server,";		//保留域名，不给会员申请的
	
	public static Map<String, Map<String, String>> templateVarMap = new HashMap<String, Map<String,String>>(); 	//模版变量缓存，模版变量都会缓存在这里面，使用时，get("模版名字").get("模版下的模版变量名字") = 模版变量内容
	
	public static final int REG_GENERAL_OSS_HAVE = 1000;	//普通注册成为会员后，拥有1000MB的存储空间
	public static final int AGENCY_SILVER_REG_OSSHAVE = 1024;		//银牌代理
	
	//站点网址，如 http://wang.market/
//	public static String masterSiteUrl = "";
	
	//阿里云短信发送
	public static AliyunSMSUtil aliyunSMSUtil;
	public static String AliyunSMS_SignName = "网市场";	//短信签名
	public static String AliyunSMS_Login_TemplateCode = "";	//登陆的短信模版
	public static String AliyunSMS_agencySiteSizeRecharge_TemplateCode = "";	//向代理商帐户转移站币，通知接收站币的乙方
	public static String AliyunSMS_siteYanQi_templateCode = "";	//网站使用期限延期(续费)后提醒对方的短信模板代码
	
	public static List<String> wangMarketDomainServerList = new ArrayList<String>();
	
	/**
	 * 持久化云端模版列表，数据来源于 http://res.weiunity.com/cloudControl/cmsTemplate.json
	 * <br/>key：list.name
	 * <br/>value：list.intro
	 */
	public static Map<String, String> cloudTemplateMap;
	
	//webSocket IM的对接url
	public static String websocketUrl = "";
	
	static{
		ConfigManagerUtil c = ConfigManagerUtil.getSingleton("wangMarketConfig.xml");
		
		VERSION = c.getValue("version");
		logger.info("version:"+VERSION);
//		masterSiteUrl = c.getValue("masterSiteUrl");
//		logger.info("masterSiteUrl:"+masterSiteUrl);
		
		
		//加载日志服务
		String smsLog = c.getValue("AliyunSMSUtil.use");
		if(smsLog != null && smsLog.equals("true")){
			String sms_accessKeyId = c.getValue("AliyunSMSUtil.accessKeyId");
			String sms_accessKeySecret = c.getValue("AliyunSMSUtil.accessKeySecret");
			if(sms_accessKeyId == null || sms_accessKeyId.length() == 0){
				//取数据库的
				sms_accessKeyId = Global.get("ALIYUN_ACCESSKEYID");
			}
			if(sms_accessKeySecret == null || sms_accessKeySecret.length() == 0){
				//取数据库的
				sms_accessKeySecret = Global.get("ALIYUN_ACCESSKEYSECRET");
			}
			if(sms_accessKeyId.length() < 10){
				System.out.println("未开启Aliyun短信发送服务");
			}
			
			aliyunSMSUtil = new AliyunSMSUtil(c.getValue("AliyunSMSUtil.regionId"), sms_accessKeyId, sms_accessKeySecret);
			AliyunSMS_SignName = c.getValue("AliyunSMSUtil.signName");
			AliyunSMS_Login_TemplateCode = c.getValue("AliyunSMSUtil.login_templateCode");
			AliyunSMS_agencySiteSizeRecharge_TemplateCode = c.getValue("AliyunSMSUtil.agency_siteSizeRecharge_templateCode");
			AliyunSMS_siteYanQi_templateCode = c.getValue("AliyunSMSUtil.siteYanQi_templateCode");
		}else{
			System.out.println("未开启Aliyun短信发送服务");
		}
		
		websocketUrl = c.getValue("websocketUrl");
	}
	
	/**
	 * 轮播图再OSS上得存储路径
	 * @return
	 */
	public static String getCarouselPath(Site site){
		return "site/"+site.getId()+"/carousel/";
	}
	
	
	private static String  firstAutoAssignDomain;	//下面方法的持久化缓存
	/**
	 * 获取主域名，即 AUTO_ASSIGN_DOMAIN 配置的第一个域名
	 * 例如，Global.get("AUTO_ASSIGN_DOMAIN") 为 ： wang.market,wscso.com
	 * @return 返回如 wang.market
	 */
	public static String getFirstAutoAssignDomain(){
		if(firstAutoAssignDomain == null){
			if(Global.get("AUTO_ASSIGN_DOMAIN") != null){
				if(Global.get("AUTO_ASSIGN_DOMAIN").indexOf(",") > 0){
					//如果有多个，那么只取第一个
					String[] s = Global.get("AUTO_ASSIGN_DOMAIN").split(",");
					firstAutoAssignDomain = s[0];
				}else{
					firstAutoAssignDomain = Global.get("AUTO_ASSIGN_DOMAIN");
				}
			}
		}
		return firstAutoAssignDomain;
	}
}
