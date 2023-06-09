package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.event.EventAfterLike;
import com.ll.gramgram.base.event.EventAfterModifyAttractiveType;
import com.ll.gramgram.base.event.EventBeforeCancelLike;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        RsData canLikeRsData = canLike(member, username, attractiveTypeCode);

        if (canLikeRsData.isFail()) return canLikeRsData;

        if(canLikeRsData.getResultCode().equals("S-2")) {
            return modifyAttractive(member, username, attractiveTypeCode);
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
                .modifyUnlockDate(AppConfig.getLikeablePersonModifyUnlockDate())
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장

        // 네가 좋아하는 유저의 호감표시가 생겼어.
        fromInstaMember.addFromLikeablePerson(likeablePerson);

        // 너를 좋아하는 유저의 호감표시가 생겼어.
        toInstaMember.addToLikeablePerson(likeablePerson);

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));

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
        publisher.publishEvent(new EventBeforeCancelLike(this, likeablePerson));

        if(likeablePerson == null) return RsData.of("F-1", "이미 취소되었습니다.");

        if(!Objects.equals(member.getInstaMember().getId(), likeablePerson.getFromInstaMember().getId())) {
            return RsData.of("F-2", "취소 권한이 없습니다.");
        }

        if(!likeablePerson.isModifyUnlocked()) {
            return RsData.of("F-3", "%s 후에 취소가 가능합니다.".formatted(likeablePerson.getModifyUnlockDateRemainStrHuman()));
        }

        // 너가 생성한 좋아요가 사라졌어.
        likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

        // 너가 받은 좋아요가 사라졌어.
        likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);

        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        likeablePersonRepository.delete(likeablePerson);

        return RsData.of("S-1", "%s님이 취소되었습니다.".formatted(toInstaMemberUsername));
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

    public Optional<LikeablePerson> findByFromInstaMember_usernameAndToInstaMember_username(String fromInstaMemberUsername, String toInstaMemberUsername) {
        return likeablePersonRepository.findByFromInstaMember_usernameAndToInstaMember_username(fromInstaMemberUsername, toInstaMemberUsername);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, Long id, int attractiveTypeCode) {
        Optional<LikeablePerson> likeablePersonOptional = findById(id);

        if (likeablePersonOptional.isEmpty()) {
            return RsData.of("F-1", "존재하지 않는 호감정보입니다.");
        }

        LikeablePerson likeablePerson = likeablePersonOptional.get();

        return modifyAttractive(actor, likeablePerson, attractiveTypeCode);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, LikeablePerson likeablePerson, int attractiveTypeCode) {
        RsData canModifyRsData = canModify(actor, likeablePerson);

        if (canModifyRsData.isFail()) {
            return canModifyRsData;
        }

        String oldAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();
        String username = likeablePerson.getToInstaMember().getUsername();

        modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);

        String newAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();

        return RsData.of("S-3", "%s님에 대한 호감정보를 %s에서 %s(으)로 수정되었습니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName), likeablePerson);
    }

    @Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, String username, int attractiveTypeCode) {
        // 액터가 생성한 `좋아요` 들 가져오기
        List<LikeablePerson> fromLikeablePeople = actor.getInstaMember().getFromLikeablePeople();

        LikeablePerson fromLikeablePerson = fromLikeablePeople
                .stream()
                .filter(e -> e.getToInstaMember().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (fromLikeablePerson == null) {
            return RsData.of("F-7", "호감표시를 하지 않았습니다.");
        }

        return modifyAttractive(actor, fromLikeablePerson, attractiveTypeCode);
    }


    private void modifyAttractionTypeCode(LikeablePerson likeablePerson, int attractiveTypeCode) {
        int oldAttractiveTypeCode = likeablePerson.getAttractiveTypeCode();
        RsData rsData = likeablePerson.updateAttractionTypeCode(attractiveTypeCode);

        if (rsData.isSuccess()) {
            publisher.publishEvent(new EventAfterModifyAttractiveType(this, likeablePerson, oldAttractiveTypeCode, attractiveTypeCode));
        }
    }

    public RsData canModify(Member actor, LikeablePerson likeablePerson) {
        if (!actor.hasConnectedInstaMember()) {
            return RsData.of("F-1", "먼저 본인의 인스타그램 아이디를 입력해주세요.");
        }

        InstaMember fromInstaMember = actor.getInstaMember();

        if (!Objects.equals(likeablePerson.getFromInstaMember().getId(), fromInstaMember.getId())) {
            return RsData.of("F-2", "해당 호감정보를 취소할 권한이 없습니다.");
        }

        if(!likeablePerson.isModifyUnlocked()) {
            return RsData.of("F-3", "%s 후에 수정이 가능합니다.".formatted(likeablePerson.getModifyUnlockDateRemainStrHuman()));
        }


        return RsData.of("S-1", "호감정보 취소가 가능합니다.");
    }

    public List<LikeablePerson> findByToInstaMember(InstaMember instaMember, String gender, int attractiveTypeCode, int sortCode) {
        Stream<LikeablePerson> likeablePeopleStream = instaMember.getToLikeablePeople().stream();

        if (gender != null) {
            likeablePeopleStream = likeablePeopleStream
                .filter(likeablePerson -> likeablePerson.getFromInstaMember().getGender().equals(gender));
        }

        if(attractiveTypeCode != 0) {
            likeablePeopleStream = likeablePeopleStream
                .filter(likeablePerson -> likeablePerson.getAttractiveTypeCode() == attractiveTypeCode);
        }

        likeablePeopleStream = switch (sortCode) {
            case 2 -> likeablePeopleStream
                .sorted(Comparator.comparing(LikeablePerson::getId));
            case 3 -> likeablePeopleStream
                .sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getLikes()).reversed()
                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
            case 4 -> likeablePeopleStream
                .sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getLikes())
                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
            case 5 -> likeablePeopleStream
                .sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getGender()).reversed()
                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
            case 6 -> likeablePeopleStream
                .sorted(Comparator.comparing(LikeablePerson::getAttractiveTypeCode)
                    .thenComparing(Comparator.comparing(LikeablePerson::getId).reversed()));
            default -> likeablePeopleStream;
        };

        return likeablePeopleStream.toList();
    }
}
