import { NativeModules } from 'react-native';

type MetronomeModuleType = {
  start: () => void;
  stop: () => void;

  playSound: (idx: number) => void;
  loadSound: (idx: number, resource: string) => void;

  setBPM: (newBPM: number) => void;
  getBPM: () => Promise<number>;

  setShouldPauseOnLostFocus: (shouldPause: boolean) => void;
  getShouldPauseOnLostFocus: () => Promise<number>;

  isPlaying: () => Promise<boolean>;
  isPaused: () => Promise<boolean>;
};

const { MetronomeModule } = NativeModules;

export default MetronomeModule as MetronomeModuleType;
