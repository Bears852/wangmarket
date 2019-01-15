package com.xnx3.wangmarket.admin.vo;

import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.xnx3.DateUtil;
import com.xnx3.StringUtil;
import com.xnx3.j2ee.func.Safety;
import com.xnx3.j2ee.vo.BaseVO;
import com.xnx3.wangmarket.admin.Func;
import com.xnx3.wangmarket.admin.entity.InputModel;
import com.xnx3.wangmarket.admin.entity.Site;
import com.xnx3.wangmarket.admin.entity.SiteColumn;
import com.xnx3.wangmarket.admin.entity.Template;
import com.xnx3.wangmarket.admin.vo.bean.template.TemplatePage;
import com.xnx3.wangmarket.admin.vo.bean.template.TemplateVar;

/**
 * 模版页面，模版导入，将导入的字符串转化为json，然后将json转化为此对象
 * @author 管雷鸣
 */
public class TemplateVO extends BaseVO {
	private Site currentSite;	//当前用户的站点信息
	private String text;		//导入的模版的text内容，字符串
	private List<TemplatePage> templatePageList;		//模版页面
	private List<TemplateVar> templateVarList;		//模版变量
	private List<InputModel> inputModelList;	//输入模型
	private List<SiteColumn> siteColumnList;	//栏目
	
	private String systemVersion;	// 当前系统版本号
	private int time;	//导出的时间，10为时间戳
	private String templateName;	//当前模版的名字
	private String sourceUrl;	//模版来源的网站，从那个网站导出来的，可以作为预览网站
	private String plugin;		//插件模式。如果此不为null，且长度大于1，则视为插件模式，无视templateName，可在已经有模版的CMS模式网站直接导入
	
	private Template template;	//模版数据信息，v4.7增加
	
	//v3.6增加，根据模版中的useUtf8Encode=true来识别。3.6之后的全部采用编码机制
	private boolean isUtf8Encode;	//当前是否使用utf8编码，将汉字转化为utf8字符，避免乱码
	
	public Site getCurrentSite() {
		return currentSite;
	}
	public void setCurrentSite(Site currentSite) {
		this.currentSite = currentSite;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public List<TemplatePage> getTemplatePageList() {
		return templatePageList;
	}
	public void setTemplatePageList(List<TemplatePage> templatePageList) {
		this.templatePageList = templatePageList;
	}
	public List<TemplateVar> getTemplateVarList() {
		return templateVarList;
	}
	public void setTemplateVarList(List<TemplateVar> templateVarList) {
		this.templateVarList = templateVarList;
	}
	public List<InputModel> getInputModelList() {
		return inputModelList;
	}
	public void setInputModelList(List<InputModel> inputModelList) {
		this.inputModelList = inputModelList;
	}
	public List<SiteColumn> getSiteColumnList() {
		return siteColumnList;
	}
	public void setSiteColumnList(List<SiteColumn> siteColumnList) {
		this.siteColumnList = siteColumnList;
	}
	
	public String getSystemVersion() {
		return systemVersion;
	}
	public void setSystemVersion(String systemVersion) {
		this.systemVersion = systemVersion;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public String getTemplateName() {
		return templateName;
	}
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	
	/**
	 * 导入模版内容。会自动赋予当前网站、来源、版本号、模版变量、模版页、栏目、输入模型等
	 * @param text 模版内容，字符串
	 * @return true：导入成功
	 */
	public boolean importText(String text) {
		//当前登录用户的站点信息
		currentSite = Func.getCurrentSite();	
		
		this.text = text;
		
		//将导入的模版转化为JSON对象
		JSONObject jo = JSONObject.fromObject(text);
		
		//首先判断是否是使用UTF8将字符串进行编码了，一定在最前面，不然后面的提取字符串是要判断是否使用编码，要解码的
		if(jo.get("useUtf8Encode") != null){
			String utf8Encode = jo.getString("useUtf8Encode");
			if(utf8Encode.equals("true")){
				this.isUtf8Encode = true;
			}
		}
		
		//TemplatePage模版页
		templatePageList = new ArrayList<TemplatePage>();
		if(jo.get("templatePageList") != null){
			JSONArray templatePageArray = jo.getJSONArray("templatePageList");
			for (int i = 0; i < templatePageArray.size(); i++) {
				JSONObject j = templatePageArray.getJSONObject(i);
				com.xnx3.wangmarket.admin.entity.TemplatePage ntp = new com.xnx3.wangmarket.admin.entity.TemplatePage();
				ntp.setName(StringUtil.filterXss(getJsonString(j.getString("name"))));
				ntp.setSiteid(currentSite.getId());
				ntp.setTemplateName(StringUtil.filterXss(getJsonString(jo.getString("templateName"))));
				ntp.setType((short) j.getInt("type"));
				ntp.setUserid(currentSite.getUserid());
				if(j.get("remark") != null){
					//兼容之前没有remark导出的json数据
					ntp.setRemark(StringUtil.filterXss(getJsonString(j.getString("remark"))));
				}
				if(j.get("editMode") != null){
					//兼容v4.4之前的版本，v4.4版本增加了templatePage.editMode字段
					ntp.setEditMode((short) j.getInt("editMode"));
				}else{
					//若没有，则默认是智能模式
					ntp.setEditMode(com.xnx3.wangmarket.admin.entity.TemplatePage.EDIT_MODE_VISUAL);
				}
				
				TemplatePage tp = new TemplatePage();
				tp.setTemplatePage(ntp);
				tp.setText(getJsonString(j.getString("text")));
				
				templatePageList.add(tp);
			}
		}
		
		

		//创建TemplateVar模版变量
		templateVarList = new ArrayList<TemplateVar>();
		if(jo.get("templateVarList") != null){
			JSONArray templateVarArray = jo.getJSONArray("templateVarList");
			for (int i = 0; i < templateVarArray.size(); i++) {
				JSONObject j = templateVarArray.getJSONObject(i);
				com.xnx3.wangmarket.admin.entity.TemplateVar tv = new com.xnx3.wangmarket.admin.entity.TemplateVar();
				tv.setAddtime(DateUtil.timeForUnix10());
				tv.setRemark(StringUtil.filterXss(getJsonString(j.getString("remark"))));
				tv.setTemplateName(StringUtil.filterXss(getJsonString(jo.getString("templateName"))));
				tv.setUpdatetime(tv.getAddtime());
				tv.setUserid(currentSite.getUserid());
				tv.setVarName(StringUtil.filterXss(getJsonString(j.getString("var_name"))));
				tv.setSiteid(currentSite.getId());
				
				TemplateVar t = new TemplateVar();
				t.setTemplateVar(tv);
				t.setText(getJsonString(j.getString("text")));
				
				templateVarList.add(t);
			}
		}
		
		

		//导入自定义输入模型
		inputModelList = new ArrayList<InputModel>();
		if(jo.get("inputModelList") != null){
			//有输入模型，那么要导入进去
			JSONArray inputModelArray = jo.getJSONArray("inputModelList");
			for (int i = 0; i < inputModelArray.size(); i++) {
				JSONObject j = inputModelArray.getJSONObject(i);
				InputModel im = new InputModel();
				im.setCodeName(StringUtil.filterXss(getJsonString(j.getString("codeName"))));
				im.setRemark(StringUtil.filterXss(getJsonString(j.getString("remark"))));
				im.setSiteid(currentSite.getId());
				im.setText(getJsonString(j.getString("text")));
				inputModelList.add(im);
			}
		}
		
		//拿到模版网站下所有可用的栏目
		siteColumnList = new ArrayList<SiteColumn>();
		if(jo.get("siteColumnList") != null){
			JSONArray siteColumnArray = jo.getJSONArray("siteColumnList");
			for (int i = 0; i < siteColumnArray.size(); i++) {
				JSONObject j = siteColumnArray.getJSONObject(i);	//要复制的目标栏目
				Short type = (short) j.getInt("type");
				
				//创建栏目，将栏目复制一份，再当前网站创建栏目
				SiteColumn nsc = new SiteColumn();
				nsc.setName(StringUtil.filterXss(getJsonString(j.getString("name"))));
				nsc.setRank(j.getInt("rank"));
				nsc.setUsed((short) j.getInt("used"));
				nsc.setSiteid(currentSite.getId());
				nsc.setUserid(currentSite.getUserid());
				nsc.setType(type);
				nsc.setTemplatePageListName(StringUtil.filterXss(getJsonString(j.getString("templatePageListName"))));
				nsc.setTemplatePageViewName(StringUtil.filterXss(getJsonString(j.getString("templatePageViewName"))));
				nsc.setCodeName(StringUtil.filterXss(getJsonString(j.getString("codeName"))));
				nsc.setParentCodeName(StringUtil.filterXss(getJsonString(j.getString("parentCodeName"))));
				nsc.setListNum(j.getInt("listNum"));
				nsc.setEditMode((short) (j.get("editMode") == null ? 0:j.getInt("editMode")));
				if(j.get("inputModelCodeName") != null){
					//兼容之前没有输入模型导出的模板
					nsc.setInputModelCodeName(StringUtil.filterXss(getJsonString(j.getString("inputModelCodeName"))));
				}
				if(j.get("listRank") != null){
					nsc.setListRank((short) j.getInt("listRank"));
				}else{
					//默认是按时间倒序，发布时间用越晚，越靠前
					nsc.setListRank(SiteColumn.LIST_RANK_ADDTIME_ASC);
				}
				
				//v4.6更新，兼容旧版本的模版，将栏目类型进行替换，替换为最新4.6版本的栏目类型
				if(nsc.getType() - SiteColumn.TYPE_NEWS == 0){
					nsc.setEditUseText(SiteColumn.USED_ENABLE);
					nsc.setType(SiteColumn.TYPE_LIST);
				}
				if(nsc.getType() - SiteColumn.TYPE_IMAGENEWS == 0){
					nsc.setEditUseText(SiteColumn.USED_ENABLE);
					nsc.setEditUseTitlepic(SiteColumn.USED_ENABLE);
					nsc.setType(SiteColumn.TYPE_LIST);
				}
				if(nsc.getType() - SiteColumn.TYPE_PAGE == 0){
					nsc.setEditUseText(SiteColumn.USED_ENABLE);
					nsc.setType(SiteColumn.TYPE_ALONEPAGE);
				}
				//v4.6版本增加的四个内容管理是否可输入项
				if(j.get("editUseTitlepic") != null){
					nsc.setEditUseTitlepic((short) j.getInt("editUseTitlepic"));
				}
				if(j.get("editUseIntro") != null){
					nsc.setEditUseIntro((short) j.getInt("editUseIntro"));
				}
				if(j.get("editUseText") != null){
					nsc.setEditUseText((short) j.getInt("editUseText"));
				}
				if(j.get("editUseExtendPhotos") != null){
					nsc.setEditUseExtendPhotos((short) j.getInt("editUseExtendPhotos"));
				}
				//v4.7，增加是否生成内容页面
				if(j.get("useGenerateView") != null){
					nsc.setUseGenerateView((short) j.getInt("useGenerateView"));
				}else{
					nsc.setUseGenerateView(SiteColumn.USED_ENABLE);
				}
				if(j.get("icon") != null){
					nsc.setIcon(j.getString("icon"));
				}else{
					nsc.setIcon("");
				}
				
				
				siteColumnList.add(nsc);
			}
		}
		
		//是否是插件模式
		if(jo.get("plugin") != null){
			plugin = getJsonStringAndSafetyFilter(jo.getString("plugin"));
		}
		
		if(jo.get("templateName") != null){
			templateName = StringUtil.filterXss(getJsonString(jo.getString("templateName")));
		}else{
			templateName = "";
		}
		
		if(jo.get("systemVersion") != null){
			systemVersion = getJsonStringAndSafetyFilter(jo.getString("systemVersion"));
		}else{
			systemVersion = "";
		}
		
		if(jo.get("time") != null){
			time = jo.getInt("time");
		}
		
		if(jo.get("sourceUrl") != null){
			sourceUrl = StringUtil.filterXss(getJsonString(jo.getString("sourceUrl")));
		}else{
			sourceUrl = "";
		}
		
		
		//v4.7版本增加template相关
		if(jo.get("template") != null){
			JSONObject tempJson = jo.getJSONObject("template");
			//进一步判断是否有 template 这个对象
			if(tempJson.get("type") != null){
				template = new Template();
				template.setAddtime(tempJson.getInt("addtime"));
				template.setCompanyname(StringUtil.filterXss(getJsonString(tempJson.getString("companyname"))));
				template.setName(StringUtil.filterXss(getJsonString(tempJson.getString("name"))));
				template.setPreviewUrl(StringUtil.filterXss(getJsonString(tempJson.getString("name"))));
				template.setRemark(StringUtil.filterXss(getJsonString(tempJson.getString("remark"))));
				template.setSiteurl(StringUtil.filterXss(getJsonString(tempJson.getString("siteurl"))));
				template.setTerminalDisplay((short) tempJson.getInt("terminalDisplay"));
				template.setTerminalIpad((short) tempJson.getInt("terminalIpad"));
				template.setTerminalMobile((short) tempJson.getInt("terminalMobile"));
				template.setTerminalPc((short) tempJson.getInt("terminalPc"));
				template.setType(tempJson.getInt("type"));
				template.setUsername(StringUtil.filterXss(getJsonString(tempJson.getString("username"))));
				template.setPreviewPic(StringUtil.filterXss(getJsonString(tempJson.getString("previewPic"))));
				template.setZipDownUrl(StringUtil.filterXss(getJsonString(tempJson.getString("zipDownUrl"))));
				template.setWscsoDownUrl(StringUtil.filterXss(getJsonString(tempJson.getString("wscsoDownUrl"))));
				if(tempJson.get("iscommon") != null){
					template.setIscommon((short) tempJson.getInt("iscommon"));
				}else{
					//默认不是公共的，私有的
					template.setIscommon(Template.ISCOMMON_NO);
				}
			}
		}
		
		
		return true;
	}
	
	/**
	 * 将json获取的字符串进行UTF8编码判断，拿到原始字符串。
	 * @param text json中拿到的字符串
	 * @return 将字符串判断是否编码，若编码了，将其解码后输出
	 */
	public String getJsonString(String text){
		if(text == null){
			return "";
		}
		if(this.isUtf8Encode){
			//使用了UTF8编码，那么进行解码
			text = StringUtil.utf8ToString(text);
		}
		
		return text;
	}
	
	/**
	 * 将json取得的字符串进行UTF8编码的判断及解码、并进行安全校验
	 * @param text json取得的字符串
	 * @return
	 */
	public String getJsonStringAndSafetyFilter(String text){
		if(text == null){
			return "";
		}
		
		return Safety.filter(getJsonString(text));
	}
	public String getPlugin() {
		return plugin;
	}
	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}
	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	
}
