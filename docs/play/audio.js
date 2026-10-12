// URIKAI ブラウザ版の音。Kotlin 側 (WebAudio.kt) から window.urikaiAudio を呼ぶ。
// iOS Safari は最初のユーザー操作内で AudioContext を resume しないと鳴らないため、操作を拾って解錠する。
(function () {
  "use strict";

  var SE_NAMES = [
    "se_buy", "se_sell", "se_miss", "se_ok", "se_great", "se_perfect", "se_fantastic",
    "se_judge_great", "se_judge_perfect", "se_judge_fantastic", "se_timeup", "se_tick",
    "se_charge_i", "se_charge_ii", "se_countdown", "se_finish", "se_payout_count",
    "se_payout_clang", "se_type", "se_star", "se_stamp", "se_panel", "se_ui_select", "se_ui_confirm"
  ];
  var BGM_TRACKS = ["audio/bgm_ambient.m4a", "audio/bgm_bossa.m4a", "audio/bgm_calm.m4a"];

  var FADE_MS = 1800;
  var GAP_MS = 10000;
  var RESUME_GAP_MS = 800;

  var AC = window.AudioContext || window.webkitAudioContext;
  var ctx = AC ? new AC() : null;
  var buffers = {};

  function unlock() {
    if (!ctx) return;
    if (ctx.state !== "running") ctx.resume();
    var b = ctx.createBuffer(1, 1, 22050);
    var s = ctx.createBufferSource();
    s.buffer = b;
    s.connect(ctx.destination);
    s.start(0);
    reconcile();
  }
  ["touchend", "mousedown", "keydown"].forEach(function (t) {
    document.addEventListener(t, unlock, { capture: true, passive: true });
  });

  function loadSe(name) {
    return fetch("audio/" + name + ".wav")
      .then(function (r) { return r.arrayBuffer(); })
      .then(function (data) {
        return new Promise(function (resolve, reject) {
          ctx.decodeAudioData(data, resolve, reject);
        });
      })
      .then(function (buf) { buffers[name] = buf; })
      .catch(function () {});
  }
  if (ctx) SE_NAMES.forEach(loadSe);

  function playSe(name, rate, volume) {
    if (!ctx || ctx.state !== "running") return;
    var buf = buffers[name];
    if (!buf) return;
    var src = ctx.createBufferSource();
    src.buffer = buf;
    src.playbackRate.value = rate;
    var gain = ctx.createGain();
    gain.gain.value = volume;
    src.connect(gain);
    gain.connect(ctx.destination);
    src.start(0);
  }

  // ---- BGM: プレイ中以外・音量ON・表示中のみ。曲末フェード → 約10秒ギャップ → ランダム次曲。
  var targetVolume = 0.32;
  var foreground = true;
  var ambientAllowed = true;
  var audioEl = null;
  var bgmGain = null;
  var lastTrack = -1;
  var gapTimer = null;
  var fadeTimer = null;
  var resumeScheduled = false;

  function pageVisible() { return document.visibilityState !== "hidden"; }

  function shouldPlay() {
    return ctx && ctx.state === "running" && targetVolume > 0.001 &&
      foreground && ambientAllowed && pageVisible();
  }

  function clearTimers() {
    if (gapTimer) { clearTimeout(gapTimer); gapTimer = null; }
    if (fadeTimer) { clearTimeout(fadeTimer); fadeTimer = null; }
    resumeScheduled = false;
  }

  function stopTrack() {
    if (fadeTimer) { clearTimeout(fadeTimer); fadeTimer = null; }
    if (audioEl) {
      audioEl.onended = null;
      audioEl.ontimeupdate = null;
      try { audioEl.pause(); } catch (e) {}
      audioEl.removeAttribute("src");
      try { audioEl.load(); } catch (e) {}
    }
  }

  function ensureElement() {
    if (audioEl) return;
    audioEl = new Audio();
    audioEl.preload = "auto";
    audioEl.setAttribute("playsinline", "");
    var src = ctx.createMediaElementSource(audioEl);
    bgmGain = ctx.createGain();
    bgmGain.gain.value = 0;
    src.connect(bgmGain);
    bgmGain.connect(ctx.destination);
  }

  function pickTrack() {
    if (BGM_TRACKS.length <= 1) return 0;
    var next;
    do { next = Math.floor(Math.random() * BGM_TRACKS.length); } while (next === lastTrack);
    return next;
  }

  function scheduleNext(delay) {
    if (gapTimer) clearTimeout(gapTimer);
    resumeScheduled = true;
    gapTimer = setTimeout(function () {
      gapTimer = null;
      resumeScheduled = false;
      if (shouldPlay()) startTrack();
    }, delay);
  }

  function startTrack() {
    ensureElement();
    stopTrack();
    var index = pickTrack();
    lastTrack = index;
    var now = ctx.currentTime;
    bgmGain.gain.cancelScheduledValues(now);
    bgmGain.gain.setValueAtTime(targetVolume, now);
    audioEl.src = BGM_TRACKS[index];
    audioEl.onended = function () {
      stopTrack();
      if (shouldPlay()) scheduleNext(GAP_MS);
    };
    audioEl.ontimeupdate = function () {
      var d = audioEl.duration;
      if (!isFinite(d) || fadeTimer) return;
      var left = (d - audioEl.currentTime) * 1000;
      if (left <= FADE_MS) {
        var t = ctx.currentTime;
        bgmGain.gain.cancelScheduledValues(t);
        bgmGain.gain.setValueAtTime(bgmGain.gain.value, t);
        bgmGain.gain.linearRampToValueAtTime(0, t + left / 1000);
        fadeTimer = setTimeout(function () { fadeTimer = null; }, left + 100);
      }
    };
    var p = audioEl.play();
    if (p && p.catch) {
      p.catch(function () {
        stopTrack();
        if (shouldPlay()) scheduleNext(GAP_MS);
      });
    }
  }

  function fadeOutFast() {
    if (!audioEl || !bgmGain || audioEl.paused) return;
    var t = ctx.currentTime;
    bgmGain.gain.cancelScheduledValues(t);
    bgmGain.gain.setValueAtTime(bgmGain.gain.value, t);
    bgmGain.gain.linearRampToValueAtTime(0, t + 0.25);
    setTimeout(stopTrack, 300);
  }

  function isPlaying() { return audioEl && !audioEl.paused && audioEl.getAttribute("src"); }

  function reconcile() {
    if (shouldPlay()) {
      if (!isPlaying() && !resumeScheduled) scheduleNext(RESUME_GAP_MS);
    } else {
      clearTimers();
      fadeOutFast();
    }
  }

  document.addEventListener("visibilitychange", function () {
    if (ctx) {
      if (pageVisible()) ctx.resume(); else ctx.suspend();
    }
    reconcile();
  });

  window.urikaiAudio = {
    playSe: playSe,
    setBgmVolume: function (v) {
      targetVolume = Math.max(0, Math.min(1, v));
      if (bgmGain && isPlaying() && !fadeTimer) {
        bgmGain.gain.setValueAtTime(targetVolume, ctx.currentTime);
      }
      reconcile();
    },
    setForeground: function (b) { foreground = b; reconcile(); },
    setAmbientAllowed: function (b) { ambientAllowed = b; reconcile(); },
    state: function () {
      return {
        context: ctx ? ctx.state : "none",
        seLoaded: Object.keys(buffers).length,
        bgmPlaying: !!isPlaying(),
        bgmSrc: audioEl ? audioEl.getAttribute("src") : null,
        bgmGain: bgmGain ? bgmGain.gain.value : 0
      };
    },
    vibrate: function (pattern) {
      if (navigator.vibrate) { try { navigator.vibrate(pattern); } catch (e) {} }
    }
  };
})();
