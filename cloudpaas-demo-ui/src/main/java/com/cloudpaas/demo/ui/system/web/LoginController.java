/**
 * 
 */
package com.cloudpaas.demo.ui.system.web;

import java.awt.image.BufferedImage;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import com.cloudpaas.common.constants.CommonConstants;
import com.cloudpaas.common.model.User;
import com.cloudpaas.common.result.BaseResponse;
import com.cloudpaas.common.utils.ErrorCode;
import com.cloudpaas.demo.ui.utils.CodeUtil;
import com.cloudpaas.plugin.redis.anno.CacheClear;
import com.cloudpaas.plugin.redis.anno.CacheWrite;
import com.cloudpaas.plugin.redis.service.IRedisService;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;

/**
 * @author 大鱼
 *
 * @date 2019年8月15日 下午2:53:55
 */
@Controller
@RequestMapping("/")
public class LoginController {
	
	private static final Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private StringRedisTemplate template;
	
	@Autowired
	private IRedisService redisService;
	
	@Autowired
    private Producer captchaProducer = null;

    @RequestMapping("/")
	public String index(){
		return "redirect:/login.html";
	}
	
	@RequestMapping("/login.html")
	public String login(){
		return "admin/login";
	}
	
	@RequestMapping(value="/login.json",method = RequestMethod.POST)
	@ResponseBody
    public BaseResponse login(HttpServletRequest request,String username,String password,String captcha){
		
		log.info("----------------"+username+","+captcha+"---------------------");
		if(!CodeUtil.checkVerifyCode(request)){
			return new BaseResponse(ErrorCode.LOGINCODEEX.getCode(),"验证码错误");
		}
		try{
			Subject subject = SecurityUtils.getSubject();
			UsernamePasswordToken token = new UsernamePasswordToken(username, password);
			subject.login(token);
		}catch (UnknownAccountException e) {
			return new BaseResponse(ErrorCode.ACCOUNTEX.getCode(),e.getMessage());
		}catch (LockedAccountException e) {
			return new BaseResponse(ErrorCode.LOGINLOCKEX.getCode(),"账号已被锁定,请联系管理员");
		}catch (IncorrectCredentialsException e) {
			return new BaseResponse(ErrorCode.LOGINPASSEX.getCode(),"密码不正确");
		}catch (AuthenticationException e) {
			return new BaseResponse(ErrorCode.AUTHEX.getCode(),"账户验证失败");
		}
		log.info("登录成功");
		return new BaseResponse(0,"登录成功");
	}
	

	@RequestMapping(value="/loginout.html")
	public String loginOut(){
		Session session = SecurityUtils.getSubject().getSession();
		session.removeAttribute(CommonConstants.USER_SESSION_ID);
		SecurityUtils.getSubject().logout();
		log.info("退出成功");
		return "redirect:/login.html";
	}
	
	@RequestMapping("/kaptcha")
    public void getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        //生成验证码
        String capText = captchaProducer.createText();
        session.setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);
        //向客户端写出
        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }
	
	@RequestMapping(value="/icon.html")
	public String icon(){
		
		return "admin/icon";
	}
	

}
