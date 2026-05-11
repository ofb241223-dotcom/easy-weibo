<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';

export interface CharacterConfig {
  scale?: number;
  mouseSensitivityX?: number;
  mouseSensitivityY?: number;
  bodySkewSensitivity?: number;
  pupilFollowFactor?: number;
  blinkMinInterval?: number;
  blinkRandomRange?: number;
  blinkDuration?: number;
  transitionDuration?: number;
  purpleColor?: string;
  blackColor?: string;
  orangeColor?: string;
  yellowColor?: string;
  errorRecoverDelay?: number;
  peekMinInterval?: number;
  peekRandomRange?: number;
}

const props = withDefaults(
  defineProps<{
    config?: CharacterConfig;
    focusedField?: 'none' | 'email' | 'password' | 'other';
    isPasswordVisible?: boolean;
    passwordLength?: number;
    isLoginError?: boolean;
    isTyping?: boolean;
  }>(),
  {
    config: () => ({}),
    focusedField: 'none',
    isPasswordVisible: false,
    passwordLength: 0,
    isLoginError: false,
    isTyping: false,
  },
);

const c = computed(() => ({
  scale: props.config.scale ?? 0.88,
  mouseSensitivityX: props.config.mouseSensitivityX ?? 20,
  mouseSensitivityY: props.config.mouseSensitivityY ?? 30,
  bodySkewSensitivity: props.config.bodySkewSensitivity ?? 120,
  pupilFollowFactor: props.config.pupilFollowFactor ?? 0.35,
  blinkMinInterval: props.config.blinkMinInterval ?? 2500,
  blinkRandomRange: props.config.blinkRandomRange ?? 3500,
  blinkDuration: props.config.blinkDuration ?? 140,
  transitionDuration: props.config.transitionDuration ?? 0.7,
  purpleColor: props.config.purpleColor ?? '#6c3ff5',
  blackColor: props.config.blackColor ?? '#171717',
  orangeColor: props.config.orangeColor ?? '#ff8433',
  yellowColor: props.config.yellowColor ?? '#e6ca0a',
  errorRecoverDelay: props.config.errorRecoverDelay ?? 2000,
  peekMinInterval: props.config.peekMinInterval ?? 2000,
  peekRandomRange: props.config.peekRandomRange ?? 3000,
}));

const wrapRef = ref<HTMLElement | null>(null);
const mouseX = ref(0);
const mouseY = ref(0);

const isPurpleBlinking = ref(false);
const isBlackBlinking = ref(false);
const isOrangeBlinking = ref(false);
const isYellowBlinking = ref(false);
let purpleBlinkTimer: number | null = null;
let blackBlinkTimer: number | null = null;
let orangeBlinkTimer: number | null = null;
let yellowBlinkTimer: number | null = null;

const isPurplePeeking = ref(false);
let purplePeekTimer: number | null = null;

const isLookingAtEachOther = ref(false);
let lookingTimer: number | null = null;

const lastPasswordLength = ref(0);
const internalError = ref(false);
let loginErrorTimer: number | null = null;
let mouseMoveHandler: ((event: MouseEvent) => void) | null = null;

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value));
}

watch(
  () => props.isLoginError,
  (value) => {
    if (value) {
      internalError.value = true;
      if (loginErrorTimer) window.clearTimeout(loginErrorTimer);
      loginErrorTimer = window.setTimeout(() => {
        internalError.value = false;
      }, c.value.errorRecoverDelay);
    } else {
      if (loginErrorTimer) {
        window.clearTimeout(loginErrorTimer);
        loginErrorTimer = null;
      }
      internalError.value = false;
    }
  },
);

watch(
  () => props.focusedField,
  (current, previous) => {
    if (lookingTimer) {
      window.clearTimeout(lookingTimer);
      lookingTimer = null;
    }

    const wasNonText = previous === 'password' || previous === 'none';
    const isNowText = current !== 'password' && current !== 'none';

    if (wasNonText && isNowText) {
      isLookingAtEachOther.value = true;
      lookingTimer = window.setTimeout(() => {
        isLookingAtEachOther.value = false;
        lookingTimer = null;
      }, 800);
    } else {
      isLookingAtEachOther.value = false;
    }
  },
);

function clearPurplePeekTimer() {
  if (purplePeekTimer) {
    window.clearTimeout(purplePeekTimer);
    purplePeekTimer = null;
  }
}

function schedulePurplePeek() {
  clearPurplePeekTimer();
  purplePeekTimer = window.setTimeout(() => {
    isPurplePeeking.value = true;
    purplePeekTimer = window.setTimeout(() => {
      isPurplePeeking.value = false;
      if (props.isPasswordVisible && props.passwordLength > 0) schedulePurplePeek();
    }, 800);
  }, Math.random() * c.value.peekRandomRange + c.value.peekMinInterval);
}

watch([() => props.isPasswordVisible, () => props.passwordLength], ([visible, length]) => {
  if (visible && length > 0) {
    schedulePurplePeek();
  } else {
    clearPurplePeekTimer();
    isPurplePeeking.value = false;
  }
});

watch(
  () => props.passwordLength,
  (value) => {
    if (value > 0) lastPasswordLength.value = value;
  },
);

function attachMouse() {
  if (mouseMoveHandler) return;
  mouseMoveHandler = (event: MouseEvent) => {
    mouseX.value = event.clientX;
    mouseY.value = event.clientY;
  };
  window.addEventListener('mousemove', mouseMoveHandler);
}

function detachMouse() {
  if (!mouseMoveHandler) return;
  window.removeEventListener('mousemove', mouseMoveHandler);
  mouseMoveHandler = null;
}

function setDefaultMouse() {
  if (!wrapRef.value) return;
  const rect = wrapRef.value.getBoundingClientRect();
  mouseX.value = rect.left + rect.width * 0.5;
  mouseY.value = rect.top + rect.height * 0.48;
}

function startBlinking() {
  const schedule = (setter: (value: boolean) => void, setTimer: (value: number) => void) => {
    const timer = window.setTimeout(() => {
      setter(true);
      window.setTimeout(() => {
        setter(false);
        schedule(setter, setTimer);
      }, c.value.blinkDuration);
    }, Math.random() * c.value.blinkRandomRange + c.value.blinkMinInterval);

    setTimer(timer);
  };

  schedule((value) => (isPurpleBlinking.value = value), (value) => (purpleBlinkTimer = value));
  schedule((value) => (isBlackBlinking.value = value), (value) => (blackBlinkTimer = value));
  schedule((value) => (isOrangeBlinking.value = value), (value) => (orangeBlinkTimer = value));
  schedule((value) => (isYellowBlinking.value = value), (value) => (yellowBlinkTimer = value));
}

const isRevealState = computed(() => props.passwordLength > 0 && props.isPasswordVisible);
const isNonPasswordFocused = computed(
  () => props.focusedField !== 'password' && props.focusedField !== 'none',
);
const isTiltState = computed(
  () => isNonPasswordFocused.value || (props.passwordLength > 0 && !props.isPasswordVisible),
);

const mouseOffset = computed(() => {
  if (!wrapRef.value) return { faceX: 0, faceY: 0, bodySkew: 0 };

  const rect = wrapRef.value.getBoundingClientRect();
  const centerX = rect.left + rect.width / 2;
  const centerY = rect.top + rect.height / 3;
  const deltaX = mouseX.value - centerX;
  const deltaY = mouseY.value - centerY;

  return {
    faceX: clamp(deltaX / c.value.mouseSensitivityX, -15, 15),
    faceY: clamp(deltaY / c.value.mouseSensitivityY, -10, 10),
    bodySkew: clamp(-deltaX / c.value.bodySkewSensitivity, -6, 6),
  };
});

const purpleStyle = computed(() => {
  const { bodySkew } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) {
    return { transform: 'translateX(0px)', height: '440px' };
  }
  if (isRevealState.value) {
    return { transform: 'skewX(0deg)', height: isNonPasswordFocused.value ? '460px' : '420px' };
  }
  if (isTiltState.value) {
    return { transform: `skewX(${bodySkew - 12}deg) translateX(40px)`, height: '460px' };
  }
  return { transform: `skewX(${bodySkew}deg)`, height: '420px' };
});

const blackStyle = computed(() => {
  const { bodySkew } = mouseOffset.value;
  if (isRevealState.value) return { transform: 'skewX(0deg)', height: '290px' };
  if (isLookingAtEachOther.value) return { transform: 'skewX(10deg) translateX(15px)', height: '290px' };
  if (isTiltState.value) return { transform: `skewX(${bodySkew * 1.5}deg)`, height: '290px' };
  return { transform: `skewX(${bodySkew}deg)`, height: '290px' };
});

const orangeStyle = computed(() => {
  if (internalError.value && !props.isPasswordVisible) {
    return {
      transform: 'skewX(-4deg) scaleX(0.96) scaleY(0.92)',
      transformOrigin: 'bottom center',
      borderRadius: '92px 166px 0 0',
    };
  }

  if (isRevealState.value) {
    return {
      transform: 'skewX(0deg) scaleX(1) scaleY(1)',
      borderRadius: '120px 120px 0 0',
      transformOrigin: 'bottom center',
    };
  }

  const { faceX, faceY } = mouseOffset.value;
  const squash = faceY * 0.011;
  const pull = Math.abs(faceX) * 0.004;
  const scaleX = 1 + squash + pull;
  const scaleY = 1 - squash - pull * 0.45;
  const skew = -faceX * 0.56;
  const leftRadius = clamp(138 + faceX * 3.5 + faceY * 1.1, 100, 200);
  const rightRadius = clamp(138 - faceX * 3.5 + faceY * 1.1, 100, 200);

  return {
    transform: `skewX(${skew.toFixed(2)}deg) scaleX(${scaleX.toFixed(3)}) scaleY(${scaleY.toFixed(3)})`,
    transformOrigin: 'bottom center',
    borderRadius: `${leftRadius}px ${rightRadius}px 0 0`,
  };
});

const yellowStyle = computed(() => {
  if (isRevealState.value) return { transform: 'skewX(0deg)' };
  return { transform: `skewX(${mouseOffset.value.bodySkew}deg)` };
});

const purpleEyesStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) return { left: `${195 + faceX}px`, top: `${38 + faceY}px` };
  if (isRevealState.value) return { left: '131px', top: '35px' };
  if (isLookingAtEachOther.value) return { left: '166px', top: '65px' };
  return { left: `${156 + faceX}px`, top: `${40 + faceY}px` };
});

const blackEyesStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (isRevealState.value) return { left: '20px', top: '28px' };
  if (isLookingAtEachOther.value) return { left: '42px', top: '12px' };
  return { left: `${46 + faceX}px`, top: `${32 + faceY}px` };
});

const orangeEyesStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) {
    return {
      left: `${153 + faceX * 1.8}px`,
      top: `${100 + faceY * 1.3 - faceX * 0.18}px`,
      transform: `rotate(${(faceX * 0.34).toFixed(2)}deg)`,
    };
  }
  if (isRevealState.value) {
    return {
      left: `${36 + faceX * 0.7}px`,
      top: `${88 + faceY * 0.6 - faceX * 0.14}px`,
      transform: `rotate(${(faceX * 0.28).toFixed(2)}deg)`,
    };
  }
  return {
    left: `${85 + faceX * 2.4}px`,
    top: `${90 + faceY * 1.45 - faceX * 0.24}px`,
    transform: `rotate(${(faceX * 0.42).toFixed(2)}deg)`,
  };
});

const yellowEyesStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (isRevealState.value) return { left: '22px', top: '39px' };
  return { left: `${59 + faceX}px`, top: `${55 + faceY}px` };
});

const purpleMouthStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;

  if (internalError.value) {
    const base = {
      width: '26px',
      height: '13px',
      borderRadius: '50% 50% 0 0',
      background: 'transparent',
      borderTop: '3.5px solid #2d2d2d',
      transform: 'translateX(-50%)',
      opacity: '1',
    };
    if (!props.isPasswordVisible) return { ...base, left: `${215 + faceX}px`, top: `${78 + faceY}px` };
    return { ...base, left: '155px', top: '72px' };
  }

  if (isRevealState.value) {
    return {
      width: '18px',
      height: '8px',
      borderRadius: '50% 50% 0 0',
      background: '#2d2d2d',
      left: '155px',
      top: '72px',
      transform: 'translateX(-50%)',
      opacity: '1',
    };
  }

  if (isTiltState.value) {
    return {
      width: '6px',
      height: '14px',
      borderRadius: '3px',
      background: '#2d2d2d',
      left: `${180 + faceX}px`,
      top: `${82 + faceY}px`,
      transform: 'translateX(-50%)',
      opacity: '1',
    };
  }

  return {
    width: '16px',
    height: '4px',
    borderRadius: '2px',
    background: '#2d2d2d',
    left: `${180 + faceX}px`,
    top: `${80 + faceY}px`,
    transform: 'translateX(-50%)',
    opacity: '1',
  };
});

const orangeMouthStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value) {
    const base = {
      width: '24px',
      height: '12px',
      borderRadius: '50% 50% 0 0',
      background: 'transparent',
      borderTop: '3px solid #2d2d2d',
      transform: `translateX(-50%) rotate(${(faceX * 0.34).toFixed(2)}deg)`,
    };
    if (!props.isPasswordVisible) {
      return {
        ...base,
        left: `${181 + faceX * 1.8}px`,
        top: `${123 + faceY * 1.3 - faceX * 0.12}px`,
      };
    }
    return {
      ...base,
      left: `${67 + faceX * 0.9}px`,
      top: `${113 + faceY * 0.7 - faceX * 0.1}px`,
      transform: `translateX(-50%) rotate(${(faceX * 0.3).toFixed(2)}deg)`,
    };
  }

  if (isRevealState.value) {
    return {
      width: '22px',
      height: '10px',
      borderRadius: '22px 22px 0 0',
      background: '#2d2d2d',
      left: `${67 + faceX * 0.8}px`,
      top: `${113 + faceY * 0.8 - faceX * 0.14}px`,
      transform: `translateX(-50%) rotate(${(faceX * 0.3).toFixed(2)}deg)`,
    };
  }

  const isPasswordHidden = props.focusedField === 'password' && !props.isPasswordVisible;
  if (isPasswordHidden) {
    return {
      width: '12px',
      height: '12px',
      borderRadius: '50%',
      background: '#2d2d2d',
      left: `${113 + faceX * 2.1}px`,
      top: `${117 + faceY * 1.55 - faceX * 0.14}px`,
      transform: `translateX(-50%) rotate(${(faceX * 0.26).toFixed(2)}deg)`,
    };
  }

  return {
    width: '24px',
    height: '12px',
    borderRadius: '0 0 24px 24px',
    background: '#2d2d2d',
    left: `${113 + faceX * 2.2}px`,
    top: `${117 + faceY * 1.6 - faceX * 0.16}px`,
    transform: `translateX(-50%) rotate(${(faceX * 0.34).toFixed(2)}deg)`,
  };
});

const yellowMouthStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value) {
    if (!props.isPasswordVisible) {
      return {
        left: '50%',
        top: '98px',
        width: '50px',
        height: '10px',
        borderRadius: '50% 50% 0 0',
        transform: `translateX(-50%) translate(${faceX * 0.5}px, ${faceY * 0.5}px)`,
      };
    }
    return {
      left: '14px',
      top: '100px',
      width: '60px',
      height: '12px',
      borderRadius: '0',
      border: 'none',
      background:
        "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='12'%3E%3Cpath d='M0 6 Q15 0 30 6 Q45 12 60 6' stroke='%232D2D2D' stroke-width='3' fill='none'/%3E%3C/svg%3E\") no-repeat center",
      transform: 'none',
    };
  }

  if (isRevealState.value) {
    return { left: '14px', top: '100px', transform: 'none' };
  }

  return { transform: `translateX(-50%) translate(${faceX * 0.5}px, ${faceY * 0.5}px)` };
});

const pupilFollowFactor = computed(() => c.value.pupilFollowFactor);

const purplePupilStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) return { transform: 'translate3d(0px, 5px, 0)' };
  if (isRevealState.value) {
    return isPurplePeeking.value
      ? { transform: 'translate3d(4px, 5px, 0)' }
      : { transform: 'translate3d(-4px, -4px, 0)' };
  }
  if (isLookingAtEachOther.value) return { transform: 'translate3d(3px, 4px, 0)' };
  return {
    transform: `translate3d(${faceX * pupilFollowFactor.value}px, ${faceY * pupilFollowFactor.value}px, 0)`,
  };
});

const pupilWhiteStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) return { transform: 'translate3d(0px, 5px, 0)' };
  if (isRevealState.value) return { transform: 'translate3d(-4px, -4px, 0)' };
  if (isLookingAtEachOther.value) return { transform: 'translate3d(0px, -4px, 0)' };
  return {
    transform: `translate3d(${faceX * pupilFollowFactor.value}px, ${faceY * pupilFollowFactor.value}px, 0)`,
  };
});

const pupilDarkStyle = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (internalError.value && !props.isPasswordVisible) return { transform: 'translate3d(0px, 3px, 0)' };
  if (isRevealState.value) return { transform: 'translate3d(-5px, -4px, 0)' };
  return {
    transform: `translate3d(${faceX * pupilFollowFactor.value}px, ${faceY * pupilFollowFactor.value}px, 0)`,
  };
});

const purpleSunglassesStyle = computed(() => {
  const { faceX } = mouseOffset.value;
  const eyes = purpleEyesStyle.value;
  const eyesLeft = parseFloat(eyes.left as string);
  const eyesTop = parseFloat(eyes.top as string);
  const tilt = 3 + faceX * 0.25;

  if (internalError.value && !props.isPasswordVisible) {
    return {
      left: `${eyesLeft - 38}px`,
      top: `${eyesTop - 36}px`,
      transform: `perspective(300px) rotateX(12deg) rotateZ(${(tilt - 18).toFixed(2)}deg)`,
    };
  }

  return {
    left: `${eyesLeft - 42}px`,
    top: `${eyesTop - 42}px`,
    transform: `perspective(300px) rotateX(12deg) rotateZ(${tilt.toFixed(2)}deg)`,
  };
});

const blackBlushConfig = computed(() => {
  const isPasswordHidden = props.focusedField === 'password' && !props.isPasswordVisible;
  if (internalError.value && !props.isPasswordVisible) return { size: 22, opacity: 0.75, blur: 6 };
  if (isPasswordHidden) return { size: 28, opacity: 0.9, blur: 6 };
  if (isRevealState.value) return { size: 16, opacity: 0.5, blur: 4 };
  if (isNonPasswordFocused.value) return { size: 20, opacity: 0.6, blur: 5 };
  return { size: 18, opacity: 0.55, blur: 4 };
});

const blackBlushBase = computed(() => {
  const { faceX, faceY } = mouseOffset.value;
  if (isRevealState.value) return { x: 44, y: 52 };
  if (isLookingAtEachOther.value) return { x: 66, y: 36 };
  return { x: 70 + faceX, y: 56 + faceY };
});

const blackBlushLeftStyle = computed(() => {
  const { x, y } = blackBlushBase.value;
  const blushConfig = blackBlushConfig.value;

  return {
    left: `${x - 22}px`,
    top: `${y}px`,
    width: `${blushConfig.size}px`,
    height: `${Math.round(blushConfig.size * 0.65)}px`,
    opacity: String(blushConfig.opacity),
    filter: `blur(${blushConfig.blur}px)`,
    transform: 'translateX(-50%)',
  };
});

const blackBlushRightStyle = computed(() => {
  const { x, y } = blackBlushBase.value;
  const blushConfig = blackBlushConfig.value;

  return {
    left: `${x + 22}px`,
    top: `${y}px`,
    width: `${blushConfig.size}px`,
    height: `${Math.round(blushConfig.size * 0.65)}px`,
    opacity: String(blushConfig.opacity),
    filter: `blur(${blushConfig.blur}px)`,
    transform: 'translateX(-50%)',
  };
});

const cssVars = computed(() => ({
  '--ac-purple': c.value.purpleColor,
  '--ac-black': c.value.blackColor,
  '--ac-orange': c.value.orangeColor,
  '--ac-yellow': c.value.yellowColor,
  '--ac-transition': `${c.value.transitionDuration}s`,
  '--ac-pupil-transition': `${Math.max(c.value.transitionDuration, 0.75)}s`,
}));

const wrapStyle = computed(() => ({
  transform: `scale(${c.value.scale})`,
  transformOrigin: 'bottom center',
}));

onMounted(() => {
  attachMouse();
  startBlinking();
  nextTick(() => setDefaultMouse());
});

onBeforeUnmount(() => {
  detachMouse();
  clearPurplePeekTimer();
  if (lookingTimer) window.clearTimeout(lookingTimer);
  if (purpleBlinkTimer) window.clearTimeout(purpleBlinkTimer);
  if (blackBlinkTimer) window.clearTimeout(blackBlinkTimer);
  if (orangeBlinkTimer) window.clearTimeout(orangeBlinkTimer);
  if (yellowBlinkTimer) window.clearTimeout(yellowBlinkTimer);
  if (loginErrorTimer) window.clearTimeout(loginErrorTimer);
});
</script>

<template>
  <div ref="wrapRef" class="ac-wrap" :style="[wrapStyle, cssVars]">
    <div class="ac-sg-anchor" :style="purpleStyle">
      <div class="ac-pixel-sg" :style="purpleSunglassesStyle">
        <div class="psg-arm" />
        <div class="psg-lens" />
        <div class="psg-bridge" />
        <div class="psg-lens" />
        <div class="psg-arm" />
      </div>
    </div>

    <div class="ac-figure ac-purple" :class="{ 'is-error': internalError && !isPasswordVisible }" :style="purpleStyle">
      <div class="ac-eyes" :style="purpleEyesStyle">
        <span class="ac-eye" :class="{ blink: isPurpleBlinking }">
          <span v-if="!isPurpleBlinking" class="ac-pupil" :style="purplePupilStyle" />
        </span>
        <span class="ac-eye" :class="{ blink: isPurpleBlinking }">
          <span v-if="!isPurpleBlinking" class="ac-pupil" :style="purplePupilStyle" />
        </span>
      </div>
      <span class="ac-mouth" :style="purpleMouthStyle" />
    </div>

    <div class="ac-figure ac-black" :class="{ 'is-error': internalError && !isPasswordVisible }" :style="blackStyle">
      <div class="ac-eyes" :style="blackEyesStyle">
        <span class="ac-eye" :class="{ blink: isBlackBlinking }">
          <span v-if="!isBlackBlinking" class="ac-pupil ac-pupil-white" :style="pupilWhiteStyle" />
        </span>
        <span class="ac-eye" :class="{ blink: isBlackBlinking }">
          <span v-if="!isBlackBlinking" class="ac-pupil ac-pupil-white" :style="pupilWhiteStyle" />
        </span>
      </div>
      <div class="ac-blush ac-blush-l" :style="blackBlushLeftStyle" />
      <div class="ac-blush ac-blush-r" :style="blackBlushRightStyle" />
    </div>

    <div class="ac-figure ac-orange" :class="{ 'is-error': internalError && !isPasswordVisible }" :style="orangeStyle">
      <div class="ac-eyes" :style="orangeEyesStyle">
        <span class="ac-eye ac-eye-dark" :class="{ blink: isOrangeBlinking }">
          <span v-if="!isOrangeBlinking" class="ac-pupil ac-pupil-dark" :style="pupilDarkStyle" />
        </span>
        <span class="ac-eye ac-eye-dark" :class="{ blink: isOrangeBlinking }">
          <span v-if="!isOrangeBlinking" class="ac-pupil ac-pupil-dark" :style="pupilDarkStyle" />
        </span>
      </div>
      <span class="ac-mouth" :style="orangeMouthStyle" />
    </div>

    <div class="ac-figure ac-yellow" :style="yellowStyle">
      <div class="ac-eyes" :style="yellowEyesStyle">
        <span class="ac-eye ac-eye-dark" :class="{ blink: isYellowBlinking }">
          <span v-if="!isYellowBlinking" class="ac-pupil ac-pupil-dark" :style="pupilDarkStyle" />
        </span>
        <span class="ac-eye ac-eye-dark" :class="{ blink: isYellowBlinking }">
          <span v-if="!isYellowBlinking" class="ac-pupil ac-pupil-dark" :style="pupilDarkStyle" />
        </span>
      </div>
      <span class="ac-mouth" :style="yellowMouthStyle" />
    </div>
  </div>
</template>

<style scoped>
.ac-wrap {
  width: 520px;
  height: 420px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.ac-figure {
  position: absolute;
  transition:
    transform var(--ac-transition, 0.7s) ease-in-out,
    height var(--ac-transition, 0.7s) ease-in-out,
    clip-path var(--ac-transition, 0.7s) ease-in-out,
    border-radius var(--ac-transition, 0.7s) ease-in-out;
  will-change: transform, height, clip-path, border-radius;
}

.ac-purple {
  left: -20px;
  bottom: 0;
  width: 360px;
  height: 420px;
  background: var(--ac-purple, #6c3ff5);
  border-radius: 10px 10px 0 0;
  transform-origin: bottom center;
  clip-path: inset(0 68px 0 68px round 10px 10px 0 0);
}

.ac-black {
  left: 230px;
  bottom: 0;
  width: 140px;
  height: 290px;
  background: var(--ac-black, #171717);
  border-radius: 8px 8px 0 0;
}

.ac-orange {
  left: 6px;
  bottom: 0;
  width: 240px;
  height: 204px;
  background: var(--ac-orange, #ff8433);
  border-radius: 120px 120px 0 0;
  z-index: 4;
  transition:
    transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1),
    height var(--ac-transition, 0.7s) ease-in-out,
    clip-path var(--ac-transition, 0.7s) ease-in-out,
    border-radius 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.ac-yellow {
  left: 324px;
  bottom: 0;
  width: 160px;
  height: 220px;
  background: var(--ac-yellow, #e6ca0a);
  border-radius: 82px 82px 0 0;
  z-index: 4;
}

.ac-eyes {
  position: absolute;
  display: flex;
  gap: 12px;
  align-items: center;
  transition:
    left var(--ac-transition, 0.7s) ease-in-out,
    top var(--ac-transition, 0.7s) ease-in-out;
}

.ac-orange .ac-eyes {
  gap: 36px;
}

.ac-black .ac-eyes {
  z-index: 3;
}

.ac-eye {
  position: relative;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #fff;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.12);
  transition: all 0.2s ease;
  z-index: 2;
  flex: 0 0 auto;
}

.ac-eye-dark {
  background: #111 !important;
  box-shadow: none !important;
  width: 12px !important;
  height: 12px !important;
}

.ac-eye.blink {
  height: 2px !important;
  margin-top: 8px;
}

.ac-pupil {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #111;
  top: 5px;
  left: 5px;
  transition: transform var(--ac-pupil-transition, 0.75s) cubic-bezier(0.22, 0.61, 0.36, 1);
}

.ac-pupil-dark {
  top: 4px;
  left: 4px;
}

.ac-mouth {
  position: absolute;
  width: 74px;
  height: 4px;
  border-radius: 8px;
  background: #2d2d2d;
  left: 50%;
  top: 104px;
  transform: translateX(-50%);
  transition:
    transform 0.5s ease-in-out,
    left 0.5s ease-in-out,
    top 0.5s ease-in-out,
    border-radius 0.4s ease,
    width 0.4s ease,
    height 0.4s ease,
    opacity 0.3s ease,
    background 0.3s ease;
}

.ac-yellow .ac-mouth {
  width: 86px;
}

.ac-purple .ac-mouth {
  width: 16px;
  height: 4px;
  border-radius: 2px;
  background: #2d2d2d;
  top: 80px;
  left: 69px;
  transform: translateX(-50%);
  opacity: 1;
}

.ac-orange .ac-mouth {
  width: 24px;
  height: 12px;
  border-radius: 0 0 24px 24px;
  background: #2d2d2d;
  left: 109px;
  top: 121px;
  transform: translateX(-50%);
}

.ac-sg-anchor {
  position: absolute;
  left: -20px;
  bottom: 0;
  width: 360px;
  height: 420px;
  transform-origin: bottom center;
  pointer-events: none;
  z-index: 10;
  transition:
    transform var(--ac-transition, 0.7s) ease-in-out,
    height var(--ac-transition, 0.7s) ease-in-out;
}

.ac-pixel-sg {
  position: absolute;
  display: flex;
  align-items: center;
  z-index: 10;
  transition:
    left var(--ac-transition, 0.7s) ease-in-out,
    top var(--ac-transition, 0.7s) ease-in-out,
    transform 0.5s ease-in-out;
  pointer-events: none;
}

.psg-arm {
  width: 10px;
  height: 5px;
  background: #1a1a1a;
  border-radius: 2px;
  flex-shrink: 0;
}

.psg-lens {
  width: 54px;
  height: 34px;
  background: #1a1a1a;
  border-radius: 8px 8px 10px 10px;
  position: relative;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.5),
    inset 0 1px 2px rgba(255, 255, 255, 0.1);
}

.psg-lens::before {
  content: '';
  position: absolute;
  bottom: 2px;
  right: 3px;
  width: 24px;
  height: 18px;
  background: repeating-conic-gradient(#1a1a1a 0% 25%, #fff 0% 50%) 0 0 / 12px 12px;
}

.psg-lens::after {
  content: '';
  position: absolute;
  top: 3px;
  left: 10px;
  width: 16px;
  height: 10px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.25), transparent);
  border-radius: 50%;
}

.psg-bridge {
  width: 10px;
  height: 8px;
  background: #1a1a1a;
  border-radius: 4px;
  margin: 0 -2px;
  flex-shrink: 0;
}

.ac-blush {
  position: absolute;
  border-radius: 50%;
  background:
    radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.9) 0%, transparent 35%),
    radial-gradient(circle at 65% 55%, rgba(255, 255, 255, 0.55) 0%, transparent 25%),
    radial-gradient(ellipse at center, #ff5c8a 0%, #ff85a0 50%, transparent 70%);
  pointer-events: none;
  z-index: 3;
  transition:
    left var(--ac-transition, 0.7s) ease-in-out,
    top var(--ac-transition, 0.7s) ease-in-out,
    width 0.6s cubic-bezier(0.34, 1.56, 0.64, 1),
    height 0.6s cubic-bezier(0.34, 1.56, 0.64, 1),
    opacity 0.5s ease,
    filter 0.5s ease;
}

.ac-purple.is-error {
  transform: translateX(0) !important;
  height: 440px !important;
  clip-path: polygon(115px 25px, 310px 0px, 255px 168px, 280px 100%, 80px 100%, 55px 175px) !important;
}

.ac-purple.is-error .ac-pupil {
  transform: translate3d(5px, 1px, 0) !important;
}

.ac-black.is-error .ac-eye {
  clip-path: inset(40% 0 0 0) !important;
}

.ac-black.is-error .ac-pupil {
  transform: translate3d(5px, 1px, 0) !important;
}

.ac-orange.is-error .ac-eye {
  transform: none !important;
}

.ac-orange.is-error .ac-mouth {
  width: 24px !important;
  height: 12px !important;
  background: transparent !important;
  border: none !important;
  border-top: 3px solid #2d2d2d !important;
  border-radius: 50% 50% 0 0 !important;
}

@media (max-width: 1100px) {
  .ac-wrap {
    transform-origin: center bottom;
  }
}
</style>
