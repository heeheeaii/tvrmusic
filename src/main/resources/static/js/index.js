const constV = {
    img: {
        pause: '/icons/play.png',
        forward: "/icons/forward.png",
        backward: "/icons/backward.png",
        background: "/images/tree.png",
        play: "/icons/play.png"
    },
    text: {
        songArtis: "song Artist",
        songName: "song Name"
    }
}
const background = document.querySelector('#background'); // background derived from album cover below
const thumbnail = document.querySelector('#thumbnail'); // album cover
const songArtist = document.querySelector('.song-artist'); // element where track artist appears
const songTitle = document.querySelector('.song-title'); // element where track title appears
const progressBar = document.querySelector('#progress-bar'); // element where progress bar appears
const pPause = document.querySelector('#play-pause'); // element where play and pause image appears
const song = document.querySelector('#song');
let playing = true;

function getTrackTimeByMs(soundPlayer) {
    const allTime = Math.floor(soundPlayer.duration * 1000);
    const hasPlayTime = Math.floor(soundPlayer.currentTime * 1000);
    return {all: allTime, hasPlay: hasPlayTime};
}

function playMusic() {
    pPause.src = constV.img.play
    thumbnail.style.transform = "scale(1)"
    song.play()
    playing = true;
}

function updateProgressValue() {
    progressBar.max = song.duration;
    progressBar.value = song.currentTime;
    document.querySelector('.currentTime').innerHTML = (formatTime(Math.floor(song.currentTime)));
    document.querySelector('.durationTime').innerHTML = (formatTime(Math.floor(song.duration)));
}

function formatTime(seconds) {
    if (!seconds) {
        return ""
    }
    let min = Math.floor((seconds / 60));
    let sec = Math.floor(seconds - (min * 60));
    if (sec < 10) {
        sec = `0${sec}`;
    }
    return `${min}:${sec}`;
}

function changeProgressBar() {
    song.currentTime = progressBar.value;
}

function previousTrack() {
    fetch('/previous', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(getTrackTimeByMs(song))
    })
        .then(response => response.json())
        .then(data => {
            song.src = data.musicUrl;
            playPause(true)
        })
        .catch(error => console.error('Error fetching previous track:', error));
}

function nextTrack() {
    fetch('/next', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(getTrackTimeByMs(song))
    })
        .then(response => response.json())
        .then(data => {
            song.src = data.musicUrl;
            playPause(true)
        })
        .catch(error => console.error('Error fetching next track:', error));
}

function playPause(isChanging = false) {
    if (isChanging) {
        playMusic()
        return
    }
    if (playing) {
        pPause.src = constV.img.pause
        thumbnail.style.transform = "scale(1.15)";
        song.pause()
        playing = false;
    } else {
        playMusic()
    }
}

function changeSongName(songNameStr) {
    songTitle.innerHTML = songNameStr
}

function changeSongArtist(songArtistStr) {
    songArtist.innerHTML = songArtistStr
}

function init() {
    song.addEventListener('ended', nextTrack);
    background.src = constV.img.background
    thumbnail.src = constV.img.background
    pPause.src = constV.img.play
    setInterval(updateProgressValue, 500);
    changeSongName(constV.text.songName)
    changeSongArtist(constV.text.songArtis)
    nextTrack()
}

init()
