package com.noti.api.user.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.security.sasl.AuthenticationException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.noti.api.notice.dto.NoticeDto;
import com.noti.api.user.dto.UserDto;
import com.noti.api.users.dto.UserSearch;
import com.noti.document.Notice;
import com.noti.document.User;
import com.noti.util.DateProcess;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
	
	MongoTemplate template;
	DateProcess date;
	
	static final String DATEFORMAT = "yyyy/MM/dd";
	static String userEmail = "";
	
	/**
	 * 사용자 전체를 반환
	 */
	public List<UserDto> getUsersInfo(UserSearch userSearch) {

		Query qurey = new Query();
		 
		// 제목, 내용  검색
		if(userSearch.isSearch()) {
			qurey.addCriteria(
				Criteria.where(userSearch.getSearchOptionName())
				 		.regex(".*" +userSearch.getSearchOptionValue() + ".*") 
			);
		}
		
		// 정렬
		if(userSearch.isOrder()) {
			qurey.with(Sort.by(
				userSearch.getOrderOptionValue().equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC
				, userSearch.getOrderOptionName()
			));
		}
		
		// 페이징
		if( userSearch.getSize() > 0 ) {
			qurey.with( PageRequest.of(0, userSearch.getSize()));
			qurey.skip( (userSearch.getPage() - 1 ) * 10 );
		}
		
		return template.find(qurey, User.class).stream().map(vo -> new UserDto(vo)).collect(Collectors.toList());
	}
	
	
	/**
	 * 로그인 사용자 정보
	 */
	public UserDto getUserInfo() {
		
		if( userEmail.equals("") ) {
			return UserDto.builder()
					      .loginState(false)
					      .build();
		}
		
		UserDto dto = new UserDto(
				template.findOne(new Query(new Criteria("userEmail").is(userEmail))
						, User.class)
					);
		
		dto.setLoginState(true);
		return dto;
	}
	
	/**
	 * 로그인
	 */
	public UserDto signIn( String userEmail, String userPwd ) {
		
		if(userEmail.equals("") || userPwd.equals("") || userEmail == null || userPwd == null ) {
			return UserDto.builder().loginState(false).stateMsg("Null Value").build();
		}
		
		Query qurey = new Query();
		
		qurey.addCriteria( Criteria.where("userEmail").is(userEmail));
		qurey.addCriteria( Criteria.where("userPwd").is(userPwd));
		
		UserDto userDto = new UserDto(template.findOne(qurey, User.class));
		
		userDto.setLoginState( userDto.getUserId() == 0 ? false : true );
		
		if(userDto.getUserId() == 0) {
			userDto.setLoginState( false );
		}else {
			this.userEmail = userDto.getUserEmail();
			userDto.setLoginState( true );
		}
		return userDto;
	}
	
	/**
	 * 회원 가입
	 */
	public UserDto signUp(UserDto userDto) {
		
		if(new UserDto(template.findOne(new Query().addCriteria( Criteria.where("userEmail").is(userDto.getUserEmail())), User.class)).getUserId() != 0) {
			return UserDto.builder().loginState(false).stateMsg("Overlap Email").build();
		};
		
		long userId = template.count(new Query(), User.class) + 1;
		
		// 유니크 키 생성
		while ( userId > 0) {
			if(new UserDto(template.findOne(new Query(new Criteria("_id").is(++userId)), User.class)).getUserId() == 0) {
				break;
			} 
		} 
		userDto.setUserId(userId);
		userDto.setCreateDate(date.getTodayDate(DATEFORMAT));
		userDto.setUpdateDate(date.getTodayDate(DATEFORMAT));
		
		UserDto user = new UserDto(template.insert(userDto.toEntity()));
		user.setStateMsg("Sign Up Success");
		
		return user;
	}
	
	/**
	 * 로그 아웃
	 */
	public UserDto signOut() {
		
		this.userEmail = "";
		return UserDto.builder().stateMsg("logout success").build();
	}
	
	/**
	 * 회원정보 수정
	 */
	public UpdateResult userUpdate(long userId, UserDto updateDto) throws AuthenticationException {

		if(!getUserInfo().isLoginState()) {
			throw new AuthenticationException("No Login");
		}
		
		Criteria criteria = new Criteria("_id");
		criteria.is(userId);
		
		Update update = new Update();
		update.set("userEmail", updateDto.getUserEmail());
		update.set("userName", updateDto.getUserName());
		update.set("userPwd", updateDto.getUserPwd());
		update.set("userAdminLevel", updateDto.getUserAdminLevel());
		update.set("updateDate", date.getTodayDate(DATEFORMAT));
		
		return template.updateFirst( new Query(criteria), update, User.class ); 
	}
	
	/**
	 * 회원정보 삭제
	 */
	public DeleteResult userDelete(long userId) throws AuthenticationException{
		if(!getUserInfo().isLoginState()) {
			throw new AuthenticationException("No Login");
		}
		
		Query query = new Query(new Criteria("_id").is(userId));
		return template.remove(query,User.class);
	}
	
	/**
	 * 특정 사용자 조회
	 */
	public UserDto getUserInfo (long userId) {
		
		Criteria criteria = new Criteria("_id");
		criteria.is(userId);
		
		Query query = new Query(criteria);
		UserDto userDto = new UserDto(template.findOne(query, User.class));
		userDto.setUserPwd("");
		
		return userDto;
	}
}