package odata.service.login.util;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import persistence.User;


public class JWTFactory {
	public static final String sign_key = "hrcloud_sign_key";
	public static final String session_name = "hrcloud_user_token";
	public static String getJWT(User user) {
		Calendar cl = Calendar.getInstance();
		cl.setTime(new Date());
		cl.add(Calendar.MINUTE, 5);
		Date exd = cl.getTime();
		Map claim = new HashMap();
		user.setPassword("");
		claim.put("user", user);
		byte[] key = sign_key.getBytes();
		return Jwts.builder().setClaims(claim).setExpiration(exd).setSubject(user.getUser_id())
				.signWith(SignatureAlgorithm.HS512, key).compact();

	}
	
	public static String getJWT(User user,String Jwt){
		if(Jwt == null){
			return "error";
		};
		
		byte[] key = sign_key.getBytes();
		try {
			if(user.getUser_id().equals(Jwts.parser().setSigningKey(key).parseClaimsJws(Jwt).getBody().getSubject())){
				Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(Jwt);
				Calendar cl = Calendar.getInstance();
				cl.setTime(new Date());
				cl.add(Calendar.MINUTE, 5);
				Date exd = cl.getTime();
				Map claim = new HashMap();
				return Jwts.builder().setClaims(claims.getBody()).setExpiration(exd).setSubject(user.getUser_id())
				.signWith(SignatureAlgorithm.HS512, key).compact();

			}else{
				return "error";
			}
		    //OK, we can trust this JWT

		} catch (SignatureException e) {
			return "error";
		    //don't trust the JWT!
		}

	}
	
	public static String getJWT_USER(String Jwt){
		byte[] key = sign_key.getBytes();
	
		return Jwts.parser().setSigningKey(key).parseClaimsJws(Jwt).getBody().getSubject();
	}
}
