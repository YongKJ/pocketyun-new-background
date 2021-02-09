package com.yongkj.pocketyun_new.basic.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import java.util.regex.Pattern;


@Controller
public class BasicController {
	private Logger log = Logger.getLogger(BasicController.class); 
	/**
	 * JSON格式响应请求
	 * @param json json格式字符串
	 * @param response 响应信息
	 */
	public void writeJson(String json,HttpServletResponse response){
		PrintWriter out =null;
		try {
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
			out.print(json);
			//out.write();
			out.flush();
			out.close();
		} catch (Exception e) {
			if(out!=null){
				out.close();
			}
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
		}
	}
	/**
	 * 判断页面是否过期
	 * @param request 客户端请求
	 * @return boolean true：页面过期 ,false：页面没有过期
	 */
	public boolean sessionTimeout(HttpServletRequest request){
		HttpSession session = request.getSession();
		if(session.getAttribute("id")==null || ((String)session.getAttribute("id")).equals("")){
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 生成UUID
	 * @return UUID
	 */
	public String getUUID(){
		String uuid = UUID.randomUUID().toString();   
		uuid = uuid.replace("-", "");
		return uuid;  
	}
	/**
	 * 获取当前日期时间
	 * @return 当前日期时间
	 * @throws ParseException 
	 */
	public Date getDate() {
		SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date changedate=null;
		try {
			changedate = df.parse(df.format(new Date()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			log.error("获取当前日期时间错误：", e);
		}
		return changedate;
	}
	
	public String removeHtmlTag(String inputString) {
		if (inputString == null)
		return null;
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		java.util.regex.Pattern p_special;
		java.util.regex.Matcher m_special;
		try {
		//定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
		String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
		//定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
		String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
		// 定义HTML标签的正则表达式
		String regEx_html = "<[^>]+>";
		// 定义一些特殊字符的正则表达式 如：&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
		String regEx_special = "\\&[a-zA-Z]{1,10};";

		p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(""); // 过滤script标签
		p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(""); // 过滤style标签
		p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(""); // 过滤html标签
		p_special = Pattern.compile(regEx_special, Pattern.CASE_INSENSITIVE);
		m_special = p_special.matcher(htmlStr);
		htmlStr = m_special.replaceAll(""); // 过滤特殊标签
		
		p_special = Pattern.compile("\\s*|\t|\r|\n");
		m_special = p_special.matcher(htmlStr);
		htmlStr = m_special.replaceAll("");
		
		textStr = htmlStr;
		
		} catch (Exception e) {
		e.printStackTrace();
		}
		return textStr;// 返回文本字符串
		}

	
	public  String getStringDate(Date date) {
		  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  String dateString = formatter.format(date);
		  return dateString;
	} 
	/**
	 * 获取md5码
	 * @param str
	 * @return
	 */
	public String md5(String str) {
		if(str==null){
			return "";
		}
		// 获取摘要工具
		MessageDigest m = null;
		try {
			// MD5摘要工具
			m = MessageDigest.getInstance("MD5");
			// 更新被文搞描述的位元组
			m.update(str.getBytes("UTF8"));
			// 捕获不支持摘要异常
		} catch (NoSuchAlgorithmException e) {
			// 创建一个MD5消息文搞 的时候出错
			e.printStackTrace();
			// 捕获不支持字符集异常
		} catch (UnsupportedEncodingException e) {
			// 更新被文搞描述的位元组 的时候出错
			e.printStackTrace();
		}
		// 最后更新使用位元组的被叙述的排列,然后完成文摘计算
		byte s[] = m.digest();
		// System.out.println(s); // 输出加密后的位元组
		// 创建结果字符串缓冲
		StringBuilder result = new StringBuilder("");
		// 遍历文摘
		for (int i = 0; i < s.length; i++) {
			// 进行十六进制转换
			result.append(Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6));
		}
		// 返回加密结果
		return result.toString();

	}
}
