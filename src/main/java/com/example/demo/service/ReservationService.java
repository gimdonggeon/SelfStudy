package com.example.demo.service;

import com.example.demo.dto.ReservationResponseDto;
import com.example.demo.entity.*;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.repository.ItemRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.querydsl.core.BooleanBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.entity.QItem.item;
import static com.example.demo.entity.QReservation.reservation;
import static com.example.demo.entity.QUser.user;
import static com.example.demo.entity.ReservationStatus.PENDING;


@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final RentalLogService rentalLogService;
    private final QReservation qReservation = QReservation.reservation;

    public ReservationService(ReservationRepository reservationRepository,
                              ItemRepository itemRepository,
                              UserRepository userRepository,
                              RentalLogService rentalLogService) {
        this.reservationRepository = reservationRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.rentalLogService = rentalLogService;
    }

    // TODO: 1. 트랜잭션 이해
    @Transactional
    public void createReservation(Long itemId, Long userId, LocalDateTime startAt, LocalDateTime endAt) {
        // 쉽게 데이터를 생성하려면 아래 유효성검사 주석 처리
        List<Reservation> haveReservations = reservationRepository.findConflictingReservations(itemId, startAt, endAt);
        if (!haveReservations.isEmpty()) {
            throw new ReservationConflictException("해당 물건은 이미 그 시간에 예약이 있습니다.");
        }
        // Item 및 User 유효성 검사
        Item item = findItemById(itemId);
        User user = findUserById(userId);

        //Reservation 생성
        Reservation reservation = new Reservation(item, user, ReservationStatus.PENDING, startAt, endAt);
        Reservation savedReservation = reservationRepository.save(reservation);

        // Rantallog 저장 (예외가 발생하면 전체 트랜잭션 롤백)
        try {
            RentalLog rentalLog = new RentalLog(savedReservation, "로그 메세지", "CREATE");
            rentalLogService.save(rentalLog);
        } catch (RuntimeException e) {
            // 로그 저장 실패 시 트랜잭션 롤백
            throw new RuntimeException("Rentallog 저장 중 오류 발생", e);
    }
}


    // TODO: 3. N+1 문제
    public List<ReservationResponseDto> getReservations() {
        //fetch join을 사용하여, User와 Item을 한 번의 쿼리로 함께 가져오게 함.
        List<Reservation> reservations = reservationRepository.findAllWithUserAndItem();

        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // TODO: 5. QueryDSL 검색 개선
    public List<ReservationResponseDto> searchAndConvertReservations(Long userId, Long itemId) {

        List<Reservation> reservations = searchReservations(userId, itemId);

        return reservations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<Reservation> searchReservations(Long userId, Long itemId) {
        BooleanBuilder builder = new BooleanBuilder();

        if (userId != null) {
           builder.and(qReservation.user.id.eq(userId));
        }
        if (itemId != null) {
            builder.and(qReservation.item.id.eq(itemId));
        }
        return (List<Reservation>) reservationRepository.findAll(builder);
    }

    private ReservationResponseDto convertToDto(Reservation reservation) {
       User user = reservation.getUser();
       Item item = reservation.getItem();

       return new ReservationResponseDto(
               reservation.getId(),
               user.getNickname(),
               item.getName(),
               reservation.getStartAt(),
               reservation.getEndAt()
       );
    }

    // TODO: 7. 리팩토링
    @Transactional
    public void updateReservationStatus(Long reservationId, ReservationStatus status) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 맞는 데이터가 존재하지 않습니다."));

        switch (status) {
            case APPROVED:
                validatePendingStatus(reservation);
                reservation.updateStatus(ReservationStatus.APPROVED);
                break;
            case CANCELED:
                validateExpiredStatus(reservation);
                reservation.updateStatus(ReservationStatus.CANCELED);
                break;
            case EXPIRED:
                validatePendingStatus(reservation);
                reservation.updateStatus(ReservationStatus.EXPIRED);
                break;
            default:
                throw new IllegalArgumentException("올바르지 않은 상태: " + status);
        }
    }

    private void validatePendingStatus(Reservation reservation) {
        if (!reservation.getStatus().equals(ReservationStatus.PENDING)) {
            throw new IllegalArgumentException("PENDING 상태만 " + reservation.getStatus() + "로 변경 가능합니다.");
        }
    }

    private void validateExpiredStatus(Reservation reservation) {
        if (reservation.getStatus().equals(ReservationStatus.EXPIRED)) {
            throw new IllegalArgumentException("EXPIRED 상태인 예약은 취소할 수 없습니다.");
        }
    }

    private Item findItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 맞는 물건이 존재하지 않습니다."));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID에 맞는 사용자가 존재하지 않습니다."));
    }
}