package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        RsData canLikeRsData = canLike(member, username, attractiveTypeCode);

        if (canLikeRsData.isFail()) return canLikeRsData;

        if(canLikeRsData.getResultCode().equals("S-2")) {
            return modifyAttractive(member, username, attractiveTypeCode);
        }

        if (!member.hasConnectedInstaMember()) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 계정을 입력해주세요.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }

        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 네가 좋아하는 유저의 호감표시가 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 유저의 호감표시가 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }

    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }

    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData<LikeablePerson> delete(Member member, LikeablePerson likeablePerson) {
        if(likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        if(!Objects.equals(member.getInstaMember().getId(), likeablePerson.getFromInstaMember().getId())) {
            return RsData.of("F-2", "삭제 권한이 없습니다.");
        }

        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s님이 삭제되었습니다.".formatted(toInstaMemberUsername));
    }

    public RsData canLike(Member actor, String username, int attractiveTypeCode) {
        if(!actor.hasConnectedInstaMember()){
            return RsData.of("F-1", "먼저 본인의 인스타그램 계정을 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if(fromInstaMember.getUsername().equals(username)) {
            return RsData.of("F-2", "본인을 호감상대로 등록할 수 없습니다.");
        }

        List<LikeablePerson> fromLikeablePeople = fromInstaMember.getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if(fromLikeablePerson != null && fromLikeablePerson.getAttractiveTypeCode() == attractiveTypeCode) {
            return RsData.of("F-3", "이미 호강상대로 등록된 유저입니다.");
        }

        if (fromLikeablePerson != null) {
            return RsData.of("S-2","%s님에 대한 호감표시가 가능합니다.".formatted(username));
        }

        long likeablePersonFromMax = AppConfig.getLikeablePersonFromMax();

        if(actor.getInstaMember().getFromLikeablePeople().size() >= likeablePersonFromMax) {
            return RsData.of("F-4", "호감 표시할 수 있는 유저는 최대 %d명입니다.".formatted(likeablePersonFromMax));
        }

        return RsData.of("S-1","%s님에 대한 호감표시가 가능합니다.".formatted(username));
    }

    private RsData<LikeablePerson> modifyAttractive(Member member, String username, int attractiveTypeCode) {

        List<LikeablePerson> fromLikeablePeople = member.getInstaMember().getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        String beforeAttractiveType = fromLikeablePerson.getAttractiveTypeDisplayName();
        fromLikeablePerson.updateAttractiveTypeCode(attractiveTypeCode);
        likeablePersonRepository.save(fromLikeablePerson);
        String afterAttractiveType = fromLikeablePerson.getAttractiveTypeDisplayName();
        return RsData.of("S-3", "%s님에 대한 호감정보가 %s에서 %s(으)로 수정되었습니다.".formatted(username, beforeAttractiveType,afterAttractiveType));
    }
}
