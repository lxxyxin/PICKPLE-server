package com.pickple.server.api.moimsubmission.service;

import com.pickple.server.api.moim.domain.Moim;
import com.pickple.server.api.moim.repository.MoimRepository;
import com.pickple.server.api.moimsubmission.domain.MoimSubmission;
import com.pickple.server.api.moimsubmission.domain.MoimSubmissionState;
import com.pickple.server.api.moimsubmission.dto.request.MoimSubmitRequest;
import com.pickple.server.api.moimsubmission.repository.MoimSubmissionRepository;
import com.pickple.server.global.exception.CustomException;
import com.pickple.server.global.response.enums.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MoimSubmissionCommandService {

    private final MoimRepository moimRepository;
    private final MoimSubmissionRepository moimSubmissionRepository;

    public void createMoimSubmission(Long moimId, Long guestId, MoimSubmitRequest request) {
        Moim moim = moimRepository.findMoimByIdOrThrow(moimId);
        MoimSubmission moimSubmission = MoimSubmission.builder()
                .guestId(guestId)
                .moim(moim)
                .answerList(request.answerList())
                .accountList(request.accountList())
                .moimSubmissionState(MoimSubmissionState.PENDING_PAYMENT.getMoimSubmissionState())
                .build();
        isDuplicatedMoimSubmission(moimSubmission);
        moimSubmissionRepository.save(moimSubmission);
    }

    private void isDuplicatedMoimSubmission(MoimSubmission moimSubmission) {
        if (moimSubmissionRepository.existsByMoimAndGuestId(moimSubmission.getMoim(),
                moimSubmission.getGuestId())) {
            throw new CustomException(ErrorCode.DUPLICATION_MOIM_SUBMISSION);
        }
    }

    public void updateSubmissionState(Long moimId, List<Long> submitterIdList) {
        List<MoimSubmission> moimSubmissionList = moimSubmissionRepository.findBymoimId(moimId);

        for (MoimSubmission moimSubmission : moimSubmissionList) {
            if (submitterIdList.contains(moimSubmission.getGuestId())) {
                //신청자 상태가 승인 대기(pendingApproval)일 때 승인
                if (moimSubmission.getMoimSubmissionState()
                        .equals(MoimSubmissionState.PENDING_APPROVAL.getMoimSubmissionState())) {
                    moimSubmission.updateMoimSubmissionState(MoimSubmissionState.APPROVED.getMoimSubmissionState());
                }
            } else {
                moimSubmission.updateMoimSubmissionState(MoimSubmissionState.REJECTED.getMoimSubmissionState());
            }
            moimSubmissionRepository.save(moimSubmission);
        }
    }
}
