import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axiosInstance from "../../util/axiosInstance";
import SockJS from "sockjs-client";
import Stomp from "stompjs";

import TeamChatInput from "../../components/teamchat/TeamChatInput";
import TeamChatMessages from "../../components/teamchat/TeamChatMessages";

interface ChatMessage {
  id: string;
  userId: number;
  sender: string;
  message: string;
  roomId: string;
  timestamp: string;
  type: "CHAT" | "JOIN" | "LEAVE";
  profileImageUrl?: string;
}

interface TeamChatRoomContainerProps {
  setParticipantCount: (count: number) => void;
}

const TeamChatRoomContainer: React.FC<TeamChatRoomContainerProps> = ({ setParticipantCount }) => {
  const { id } = useParams<{ id: string }>();
  const [stompClient, setStompClient] = useState<Stomp.Client | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [comment, setComment] = useState("");
  const [connected, setConnected] = useState(false);
  const currentUserId = 123; // 실제 구현시에는 로그인한 사용자의 ID를 사용

  useEffect(() => {
    if (!id) return;

    const socket = new SockJS(`${axiosInstance.defaults.baseURL}/api-chat/ws/chat`);
    const stomp = Stomp.over(socket);

    stomp.connect({}, () => {
      setConnected(true);
      console.log("WebSocket 연결 성공");

      // 입장 메시지 전송
      const joinMessage = {
        userId: currentUserId,
        sender: "User123",
        type: "JOIN",
        roomId: id,
        message: "님이 입장하셨습니다.",
        timestamp: new Date().toISOString(),
        profileImageUrl: "/path/to/profile.jpg",
      };

      stomp.send(`/app/chat/${id}/join`, {}, JSON.stringify(joinMessage));

      // 채팅방 메시지 구독
      stomp.subscribe(`/topic/chatroom/${id}`, message => {
        const newMessage = JSON.parse(message.body);
        setMessages(prev => [...prev, newMessage]);
      });

      // 참여자 수 구독
      stomp.subscribe(`/topic/chatroom/${id}/participants`, message => {
        const count = parseInt(message.body, 10);
        setParticipantCount(count); // 참여자 수 업데이트
      });

      setStompClient(stomp);
    });

    return () => {
      if (stompClient) {
        const leaveMessage = {
          userId: currentUserId,
          sender: "User123",
          type: "LEAVE",
          roomId: id,
          message: "님이 퇴장하셨습니다.",
          timestamp: new Date().toISOString(),
        };

        stompClient.send(`/app/chat/${id}/leave`, {}, JSON.stringify(leaveMessage));
        stompClient.disconnect(() => {
          console.log("WebSocket 연결 해제");
          setConnected(false);
        });
      }
    };
  }, [id]);

  const handleSendMessage = () => {
    if (!id || !stompClient || !comment.trim()) return;

    const chatMessage = {
      userId: currentUserId,
      sender: "User123",
      message: comment,
      roomId: id,
      type: "CHAT" as const,
      timestamp: new Date().toISOString(),
      profileImageUrl: "/path/to/profile.jpg",
    };

    stompClient.send(`/app/chat/${id}/sendMessage`, {}, JSON.stringify(chatMessage));
    setComment("");
  };

  return (
    <div className="flex flex-col">
      <div className="flex-1 overflow-y-auto">
        <TeamChatMessages
          messages={messages}
          currentUserId={currentUserId}
          userImage="/path/to/profile.jpg"
        />
      </div>
      <TeamChatInput
        onSendMessage={handleSendMessage}
        comment={comment}
        setComment={setComment}
        placeholder="메시지를 입력하세요..."
        disabled={!connected}
      />
    </div>
  );
};

export default TeamChatRoomContainer;
