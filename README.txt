Find Utoob video
Download said toob: https://github.com/axet/vget.git

Strip pics from video - VideoThumbnailsExample

ModifyMediaExample to converge. Concatenate as alternative.

RuntimeException: could not open: /tmp/kompost/NORWAY-A_Time..... -> May be because the file is erronous (Lacks videostream / empty)
TODO Check out IContainer.seekKeyFrame

Changing playback rate
========================
https://trac.ffmpeg.org/wiki/How%20to%20speed%20up%20/%20slow%20down%20a%20video
//Half as large. Doesn't drop frames 30000/1001
ffmpeg -i Snowy_horse_171000000___174000000.mp4 -filter:v "setpts=0.5*PTS" Snowy_stretched_droopin.mp4

//Quarter as large. Drops frames - 16/1
ffmpeg -i Snowy_horse_171000000___174000000.mp4 -r 16 -filter:v "setpts=0.25*PTS" Snowu_stretched_video.mp4



Original Snowy Horse:
Snowy_horse_ 171000000 174000000  = 3000000 = 3000 ms eller 3 sekunder

Skal passe inn i 0-16 Beats i 128 BPM - >

128 / 16 = 8
8dels minutt
Target skal bli 7.5 sekunder


Finne ut av tid i fil:
original er
ffmpeg -i Flurries_on_roadside_einer_56222833___60477083.mp4 2>&1 | grep Duration
-->  Duration: 00:00:04.26, start: 0.000000, bitrate: 1462 kb/s

7.5 / 04.26 = 1,76056338028169


ffmpeg -i Flurries_on_roadside_einer_56222833___60477083.mp4 -filter:v "setpts=1.760563380*PTS" stretched.mp4

