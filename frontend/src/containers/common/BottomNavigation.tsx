import { useLocation, useNavigate } from "react-router-dom";
import BottomNavigationComponent from "../../components/common/BottomNavigation";
import ClubId from "../../util/ClubId";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { RootState } from "../../redux/store";

const routes = ["/club", "/cheersong", "/", "/commuhome", "/mypage"];

const getClubName = (value: number): string | undefined => {
  return Object.entries(ClubId).find(([, v]) => v === value)?.[0];
};

const BottomNavigation = () => {
  const location = useLocation();
  const nav = useNavigate();

  const { cheeringClub } = useSelector((state: RootState) => state.team);

  const [clickedButtonIndex, setClickedButtonIndex] = useState<number>(2);

  useEffect(() => {
    const currentPathName = location.pathname;

    if (currentPathName === routes[2]) {
      setClickedButtonIndex(2);
    } else {
      const clickedIndex = routes.findIndex(
        (route, index) => index !== 2 && currentPathName.startsWith(route),
      );

      if (clickedIndex !== -1) {
        setClickedButtonIndex(clickedIndex);
      }
    }
  }, [location.pathname]);

  const handleBottomNavigationClick = (index: number) => {
    if (index === 0) {
      const clubId: number = cheeringClub ? (cheeringClub > 0 ? cheeringClub : 1) : 1;
      const clubName: string | undefined = getClubName(clubId);

      if (clubName === undefined) {
        nav(`${routes[index]}/${ClubId[0]}`);
      } else {
        nav(`${routes[index]}/${clubName}`);
      }
    } else {
      nav(routes[index]);
    }
  };

  return (
    <BottomNavigationComponent
      clickedButtonIndex={clickedButtonIndex}
      onButtonClick={handleBottomNavigationClick}
    />
  );
};

export default BottomNavigation;
