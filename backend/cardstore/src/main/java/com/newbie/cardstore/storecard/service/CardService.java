package com.newbie.cardstore.storecard.service;

import com.newbie.cardstore.storecard.dto.PlayerCardDto;
import com.newbie.cardstore.storecard.entity.PlayerCard;
import com.newbie.cardstore.storecard.entity.SortType;
import com.newbie.cardstore.storecard.repository.PlayerCardRepository;
import com.newbie.cardstore.usercard.entity.UserCard;
import com.newbie.cardstore.usercard.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final PlayerCardRepository playerCardRepository;
    private final UserCardRepository userCardRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mileage.server.domain}")
    private String milesServerName;

    @Value("${server.path}")
    private String mileageServerPath;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.player}")
    private String playerRoutingKey;

    /**
     * 사용자 ID로 사용자가 구매한 카드 목록 조회
     *
     * @param memberId
     * @return List<PlayerCardDto>
     */
    public List<PlayerCardDto> getCardsByUserId(String memberId) {
        Long userId = Long.valueOf(memberId);
        Optional<UserCard> userCard = userCardRepository.findByUserId(userId);

        if (userCard.isEmpty() || userCard.get().getCardIds().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> cardIds = userCard.get().getCardIds().stream()
                .map(ObjectId::toHexString)
                .collect(Collectors.toSet());

        List<PlayerCard> playerCards = playerCardRepository.findAllById(cardIds);

        return playerCards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<PlayerCardDto> getPlayerDetailsWithCardInfo(String id) {
        return playerCardRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * 카드 목록 조회
     *
     * @param teamId, sortType, includeCard, userId
     * @return List<PlayerCardDto>
     */
    public List<PlayerCardDto> getCards(int teamId, SortType sortType, String position, boolean includeCard, String memberId) {
        List<PlayerCard> cards;
        Long userId = Long.valueOf(memberId);

        // 정렬 타입에 따라 카드를 조회
        if (position != null && !position.isEmpty()) {
            switch (sortType) {
                case LATEST:
                    cards = playerCardRepository.findByTeamAndPositionOrderByCreatedAtDesc(teamId, position);
                    break;
                case SALES:
                    cards = playerCardRepository.findByTeamAndPositionOrderBySalesCountDesc(teamId, position);
                    break;
                default:
                    cards = playerCardRepository.findByTeamAndPosition(teamId, position);
                    break;
            }
        } else {
            switch (sortType) {
                case LATEST:
                    cards = playerCardRepository.findByTeamOrderByCreatedAtDesc(teamId);
                    break;
                case SALES:
                    cards = playerCardRepository.findByTeamOrderBySalesCountDesc(teamId);
                    break;
                default:
                    cards = playerCardRepository.findByTeam(teamId);
                    break;
            }

            log.info(cards.toString());
        }
        // 사용자 소유 카드를 제외하는 필터링 로직
        if (!includeCard) {
            Set<ObjectId> userCardIds = findUserCardIds(userId);
            cards = cards.stream()
                    .filter(card -> !userCardIds.contains(new ObjectId(card.getId())))
                    .collect(Collectors.toList());
        }

        return cards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 유저의 최신 선수 카드 한 개 조회
     * @param memberId
     * @return
     */
    public PlayerCardDto getMyLatestCard(String memberId) {
        Long userId = Long.valueOf(memberId);
        Set<ObjectId> cardIds = findUserCardIds(userId);

        if (cardIds.isEmpty()) {
            return null;
        }

        List<PlayerCard> sortedCards = playerCardRepository.findByIdInOrderByCreatedAtDesc(cardIds);

        return sortedCards.isEmpty() ? null : convertToDTO(sortedCards.get(0));
    }

    /**
     * 판매순 TOP3 카드 조회
     * @return
     */
    public List<PlayerCardDto> getTopSalesCards() {
        List<PlayerCard> cards = playerCardRepository.findTop3ByOrderBySalesCountDesc();
        return cards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void deleteCard(String cardId, String memberId) {
        ObjectId objectId;
        Long userId = Long.valueOf(memberId);
        try {
            objectId = new ObjectId(cardId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cardId format: " + cardId, e);
        }

        UserCard userCard = userCardRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCard not found for userId: " + userId));

        if (!userCard.getCardIds().contains(objectId)) {
            throw new IllegalArgumentException("CardId not found in user's card list: " + cardId);
        }

        userCard.getCardIds().remove(objectId);

        userCardRepository.save(userCard);
    }

    public Double getMileage(String userId) {
        String url = milesServerName + ":" + mileageServerPath + "/mileage?userId=" + userId;
        log.info(url);

        try {
            ResponseEntity<Double> response = restTemplate.getForEntity(url, Double.class);

            if (response.getBody() != null) {
                log.info("Successfully fetched mileage for userId: {}, mileage: {}", userId, response.getBody());
                return response.getBody();
            } else {
                log.warn("Mileage response is null for userId: {}", userId);
                return 0.0;
            }
        } catch (Exception e) {
            log.error("Failed to fetch mileage for userId: {}", userId, e);
            throw new RuntimeException("Failed to fetch mileage for userId: " + userId, e);
        }
    }


    private Set<ObjectId> findUserCardIds(Long userId) {
        Set<ObjectId> objectIds = userCardRepository.findByUserId(userId)
                .map(UserCard::getCardIds)
                .orElse(Collections.emptySet());

        log.info(objectIds.toString());

        return objectIds;
    }

    // PlayerCard 엔티티를 PlayerCardDto로 변환
    private PlayerCardDto convertToDTO(PlayerCard playerCard) {
        return PlayerCardDto.builder()
                .id(playerCard.getId())
                .name(playerCard.getName())
                .no(playerCard.getNo())
                .team(playerCard.getTeam())
                .price(playerCard.getPrice())
                .imageUrl(playerCard.getImageUrl())
                .position(playerCard.getPosition())
                .createdAt(playerCard.getCreatedAt())
                .build();
    }
}

