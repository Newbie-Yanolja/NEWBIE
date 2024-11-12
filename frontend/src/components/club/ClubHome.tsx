import ClubOverview, { ClubOverviewProps } from "./ClubOverview";
import ClubRecord from "./ClubRecord";
import OtherClubs from "./OtherClubs";
import PlayerList from "./PlayerList";
import UpcomingGame from "./UpcomingGame";

interface ClubHomeProps {
  clubOverviewProps: ClubOverviewProps;
}

const ClubHome = (props: ClubHomeProps) => {
  return (
    <div className="bg-club-ssg w-full max-w-[600px] min-w-[320px] mx-auto pt-4">
      <ClubOverview {...props.clubOverviewProps} />
      <div className="bg-white w-full rounded-t-2xl pt-6 px-4 mt-6 pb-32">
        <ClubRecord />
        <UpcomingGame />
        <PlayerList />
        <OtherClubs />
      </div>
    </div>
  );
};

export default ClubHome;
