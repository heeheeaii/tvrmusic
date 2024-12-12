const audioPlayer = document.getElementById("audioPlayer");
let currentTrackIndex = 0;

function getTrackTimeByMs(soundPlayer) {
    const allTime = Math.floor(soundPlayer.duration * 1000);
    const hasPlayTime = Math.floor(soundPlayer.currentTime * 1000);
    return {all: allTime, hasPlay: hasPlayTime};
}

function playAudio() {
    audioPlayer.play();
}

function pauseAudio() {
    audioPlayer.pause();
}

function previousTrack() {
    fetch('/previous', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(getTrackTimeByMs(audioPlayer))
    })
        .then(response => response.json())
        .then(data => {
            audioPlayer.src = data.musicUrl;
            playAudio();
        })
        .catch(error => console.error('Error fetching previous track:', error));
}

function nextTrack() {
    fetch('/next', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(getTrackTimeByMs(audioPlayer))
    })
        .then(response => response.json())
        .then(data => {
            audioPlayer.src = data.musicUrl;
            playAudio();
        })
        .catch(error => console.error('Error fetching next track:', error));
}

