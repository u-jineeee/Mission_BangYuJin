package com.ll.gramgram.boundedContext.likeablePerson.controller;


import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.boundedContext.likeablePerson.service.LikeablePersonService;
import com.ll.gramgram.boundedContext.member.entity.Member;
import com.ll.gramgram.boundedContext.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class LikeablePersonControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LikeablePersonService likeablePersonService;

    @Test
    @DisplayName("등록 폼(인스타 인증을 안해서 폼 대신 메세지)")
    @WithUserDetails("user1")
    void t001() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/likeablePerson/like"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showLike"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        먼저 본인의 인스타 아이디를 입력해주세요.
                        """.stripIndent().trim())))
        ;
    }

    @Test
    @DisplayName("등록 폼")
    @WithUserDetails("user2")
    void t002() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/likeablePerson/like"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showLike"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="text" name="username"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input class="radio radio-primary peer" type="radio" name="attractiveTypeCode" value="1"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input class="radio radio-primary peer" type="radio" name="attractiveTypeCode" value="2"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input class="radio radio-primary peer" type="radio" name="attractiveTypeCode" value="3"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        id="btn-like-1"
                        """.stripIndent().trim())));
        ;
    }

    @Test
    @DisplayName("등록 폼 처리(user2가 user3에게 호감표시(외모))")
    @WithUserDetails("user2")
    void t003() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/usr/likeablePerson/like")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "insta_user3")
                        .param("attractiveTypeCode", "1")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("like"))
                .andExpect(status().is3xxRedirection())
        ;
    }

    @Test
    @DisplayName("등록 폼 처리(user2가 abcd에게 호감표시(외모), abcd는 아직 우리 서비스에 가입하지 않은상태)")
    @WithUserDetails("user2")
    void t004() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/usr/likeablePerson/like")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "abcd")
                        .param("attractiveTypeCode", "2")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("like"))
                .andExpect(status().is3xxRedirection())
        ;
    }

    @Test
    @DisplayName("호감목록")
    @WithUserDetails("user3")
    void t005() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/likeablePerson/list"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showList"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        data-test="toInstaMember_username=insta_user4"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        data-test="toInstaMember_attractiveTypeDisplayName=외모"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        data-test="toInstaMember_username=insta_user100"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        data-test="toInstaMember_attractiveTypeDisplayName=성격"
                        """.stripIndent().trim())));
    }

    @Test
    @DisplayName("호감 취소")
    @WithUserDetails("user3")
    void t006() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        post("/usr/likeablePerson/delete/1")
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usr/likeablePerson/list**"))
        ;

        assertThat(likeablePersonService.findById(1L).isPresent()).isEqualTo(false);
    }

    @Test
    @DisplayName("호감 상대 삭제(권한이 없음 -> 삭제 안됨)")
    @WithUserDetails("user2")
    void t008() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        post("/usr/likeablePerson/delete/1")
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is4xxClientError())
        ;

        assertThat(likeablePersonService.findById(1L).isPresent()).isEqualTo(true);
    }

    @Test
    @DisplayName("상대방 중복 등록 처리(user3이 user4에게 호감 중복 표시)")
    @WithUserDetails("user3")
    void t009() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/usr/likeablePerson/like")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "insta_user4")
                        .param("attractiveTypeCode", "1")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("like"))
                .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @DisplayName("호감 유저 등록 처리 최대 개수 제한")
    @WithUserDetails("user4")
    void t010() throws Exception {
        Member memberUser4 = memberService.findByUsername("user4").get();

        IntStream.range(0, (int) AppConfig.getLikeablePersonFromMax())
                .forEach(index -> {
                    likeablePersonService.like(memberUser4, "insta_user%30d".formatted(index), 1);
                });
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/usr/likeablePerson/like")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "test_user11")
                        .param("attractiveTypeCode", "1")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("like"))
                .andExpect(status().is4xxClientError())
        ;
    }

    @Test
    @DisplayName("상대방 중복 등록 수정 처리(외모 -> 성격 수정)")
    @WithUserDetails("user3")
    void t011() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(post("/usr/likeablePerson/like")
                        .with(csrf()) // CSRF 키 생성
                        .param("username", "insta_user4")
                        .param("attractiveTypeCode", "2")
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("like"))
                .andExpect(redirectedUrlPattern("/usr/likeablePerson/list**"))
        ;
    }

    @Test
    @DisplayName("수정 폼")
    @WithUserDetails("user3")
    void t012() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(get("/usr/likeablePerson/modify/2"))
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("showModify"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="1"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="2"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        <input type="radio" name="attractiveTypeCode" value="3"
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        inputValue__attractiveTypeCode = 2;
                        """.stripIndent().trim())))
                .andExpect(content().string(containsString("""
                        id="btn-modify-like-1"
                        """.stripIndent().trim())));
        ;
    }
}
