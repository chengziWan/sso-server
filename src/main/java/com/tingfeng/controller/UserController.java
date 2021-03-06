package com.tingfeng.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.tingfeng.config.CasConfig;
import com.tingfeng.utils.CasServerUtil;
import com.tingfeng.viewmodel.res.UserCheckResponse;


/**
 * 用户单点有几个概念(后期再完善)
 * 1、browser_1 登录之后，browser_2 也可以登录
 * 2、browser_1 登录之后，browser_2 不可以登录
 * 3、browser_1 登录之后，browser_2 可以登录，把 browser_1 用户强制推出
 */
@Controller
@RequestMapping("/user")
public class UserController {

    //@Autowired
    //private TgtServer tgtServer;

    /**
     * CAS 登录授权
     */
    @PostMapping("/login")
    public Object login(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String service = request.getParameter("service");

        System.out.println("username：" + username + "，password：" + password + "，service：" + service);

        // 1、获取 TGT
        String tgt = CasServerUtil.getTGT(username, password);
        System.out.println("TGT：" + tgt);

        // 2、获取 ST
        String st = CasServerUtil.getST(tgt, service);
        System.out.println("ST：" + st);

        if (tgt == null || st==null){
            return new ResponseEntity("用户名或密码错误。", HttpStatus.BAD_REQUEST);
        }

        // 3、设置cookie（1小时）
        Cookie cookie = new Cookie(CasConfig.COOKIE_NAME, username + "@" + tgt);
        cookie.setMaxAge(CasConfig.COOKIE_VALID_TIME);             // Cookie有效时间
        cookie.setPath("/");                       // Cookie有效路径
        cookie.setHttpOnly(true);                  // 只允许服务器获取cookie
        response.addCookie(cookie);

        // 4、将当前用户的TGT信息存储在Redis上
        //tgtServer.setTGT(username, tgt, CasConfig.COOKIE_VALID_TIME);

        // 5、302重定向最后授权
        String redirectUrl = service + "?ticket=" + st;
        System.out.println("redirectUrl:" + redirectUrl);

        return "redirect:" + redirectUrl;
    }

    /**
     * 检查用户是否登录过
     */
    @RequestMapping("/check")
    @ResponseBody
    public String checkLoginUser(HttpServletRequest request) throws Exception {
    	System.out.println("checkLoginUser............................");
    	
        String service = request.getParameter("service");
        String callback = request.getParameter("callback");
        Cookie[] cookies = request.getCookies();
        String username = null;
        String tgt = null;
        System.out.println("service="+service);
        UserCheckResponse result = new UserCheckResponse();

        if (cookies != null) {
            System.out.println(new Gson().toJson(cookies));

            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(CasConfig.COOKIE_NAME)) {
                    username = cookie.getValue().split("@")[0];
                    tgt = cookie.getValue().split("@")[1];
                    break;
                }
            }

          //然后根据code获取tgt，获取st
            if (username != null) {
                // 获取Redis值
               // String value = tgtServer.getTGT(username);
               // System.out.println("Redis value：" + value);

                // 匹配Redis中的TGT与Cookie中的TGT是否相等
//                if (tgt.equals(value)) {
//
//                    // 获取 ST
//                    String st = CasServerUtil.getST(tgt, service);
//                    System.out.println("ST：" + st);
//
//                    result.setStatus(1);
//                    result.setData(service + "?ticket=" + st);
//                }
        }
        
        }
        
        String code = "";
        if (service.lastIndexOf("code=") != -1) {
        	code = service.substring(service.lastIndexOf("code=")+5);
        }
        System.out.println("code="+code);
        if (code != null && code != "" && !"".equals(code)) {
            System.out.println("code----------"+code);
            String tgt1 = "TGT-8-b6MjdaLejJbtktIwnMAXeDbVvHr5d7eWaw68PI9Manlcyyh-6cUkhEuwMJQVFQuQw2QLAPTOP-9JI6HR7S";//tgtServer.getTGT(username);
            String st = CasServerUtil.getST(tgt1, service);
            System.out.println("st="+st);
            result.setStatus(1);
            result.setData(service + "&ticket="+st);
            //根据code获取accessToken
            String accessToken = CasServerUtil.getAccessToken("authorization_code", "100001abcdeft",  "100001", "http://app1.com:8181/fire/books.html", code);
            //根据accessToken获取profile
            String profile = CasServerUtil.getProfile(accessToken);
        }

        System.out.println("callback：" + callback);
        String tmp = callback + "(" + new Gson().toJson(result) + ")";
        System.out.println("result：" + tmp);

        return tmp;
    }

    /**
     * 因为TGT在SSO服务端维护，并不在CAS-Server，所以只需要想办法把redis中匹配的tgt信息删除即可。
     */
    @GetMapping("/logout")
    @ResponseBody
    public String logout(HttpServletRequest request) {
        String callback = request.getParameter("callback");
        Cookie[] cookies = request.getCookies();
        String username = null;
        String tgt = null;

        if (cookies != null) {
            System.out.println(new Gson().toJson(cookies));

            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(CasConfig.COOKIE_NAME)) {
                    username = cookie.getValue().split("@")[0];
                    tgt = cookie.getValue().split("@")[1];
                    break;
                }
            }

//            if (username != null) {
//                // 获取Redis值
//                String value = tgtServer.getTGT(username);
//                System.out.println("Redis value：" + value);
//
//                // 匹配Redis中的TGT与Cookie中的TGT是否相等
//                if (tgt.equals(value)) {
//                    // 删除TGT
//                    tgtServer.delTGT(username);
//                }
//            }
        }

        System.out.println("callback：" + callback);
        String tmp = callback + "({'code':'0','msg':'登出成功'})";
        System.out.println("result：" + tmp);

        return null;
    }
   
    
    @PostMapping("/redirectOauthCode")
    public Object redirectOauthCode(HttpServletRequest request, HttpServletResponse response) throws Exception {

    	//response_type=code&client_id=100001&redirect_uri=http://127.0.0.1:8080/client1
      String response_type = request.getParameter("response_type");
      String client_id = request.getParameter("client_id");
      String redirect_uri = request.getParameter("redirect_uri");
      String callback = request.getParameter("callback");

      System.out.println("response_type：" + response_type + "，client_id：" + client_id + "，redirect_uri：" + redirect_uri+",callback="+callback);

      // 1、获取 TGT
      //String  redirectUrl = CasServerUtil.getRedirectCodeURL(response_type, client_id,redirect_uri);
      
      //System.out.println("redirectUrl：" + redirectUrl);

        // 5、302重定向最后授权
        String redirectUrl = CasConfig.GET_OAUTH_CODE_URL+"?response_type=code&client_id=100001&redirect_uri="+redirect_uri+"&callback="+callback;
        System.out.println("redirectUrl:" + redirectUrl);

        return "redirect:" + redirectUrl;
    }

}
