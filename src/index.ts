import { NativeModules } from "react-native";

export type VisibilityStatus = "hidden" | "visible";
export type Config = { duration?: number };

const NativeModule: {
  hide: (duration: number) => Promise<true>;
  getVisibilityStatus: () => Promise<VisibilityStatus>;
} = NativeModules.RNBootSplash;

export function hide(config: Config = {}): Promise<void> {
  const { duration = 0 } = config;
  const fade = duration > 0;
  return NativeModule.hide(fade ? Math.max(duration, 220) : 0).then(() => {});
}

export function getVisibilityStatus(): Promise<VisibilityStatus> {
  return NativeModule.getVisibilityStatus();
}

export default {
  hide,
  getVisibilityStatus,
};
