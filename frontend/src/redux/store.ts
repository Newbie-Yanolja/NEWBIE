import { configureStore } from "@reduxjs/toolkit";
import userReducer from "./userSlice";
import notiReducer from "./notiSlice";

const store = configureStore({
  reducer: {
    user: userReducer,
    notification: notiReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export default store;
