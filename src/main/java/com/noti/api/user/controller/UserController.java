package com.noti.api.user.controller;

import javax.security.sasl.AuthenticationException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.noti.api.user.dto.UserDto;
import com.noti.api.user.service.UserService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("user")
@RestController
public class UserController {
   
   UserService service;
   
   @GetMapping("")
   @ResponseBody
   public ResponseEntity<UserDto> getUserInfo( ) {
      return ResponseEntity.ok().body(
         service.getUserInfo()
      );
   }
   
   @PostMapping("/sign-in")
   @ResponseBody
   public ResponseEntity<UserDto> signIn( String userEmail, String userPwd ) {
      return ResponseEntity.ok().body(
         service.signIn(userEmail, userPwd)
      );
   }
   
   @PostMapping("/sign-up")
   @ResponseBody
   public ResponseEntity<UserDto> signUp(@RequestBody UserDto userDto) {
      return ResponseEntity.ok().body(
         service.signUp(userDto)
      );
   }
   
   @PostMapping("/sign-out")
   @ResponseBody
   public ResponseEntity<UserDto> signOut( ) {
      return ResponseEntity.ok().body(
         service.signOut()
      );
   }
   
   @PatchMapping("/{userId}")
   @ResponseBody
   public ResponseEntity<UpdateResult> userUpdate(@PathVariable(name = "userId", required = true) long userId, @RequestBody UserDto updateDto) throws AuthenticationException {
      return ResponseEntity.ok().body(
         service.userUpdate(userId, updateDto)
      );
   }
   
   @DeleteMapping("/{userId}")
   @ResponseBody
   public ResponseEntity<DeleteResult> userDelete(@PathVariable(name = "userId", required = true) long userId) throws AuthenticationException {
      return ResponseEntity.ok().body(
         service.userDelete(userId)
      );
   }
}