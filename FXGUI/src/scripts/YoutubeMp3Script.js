/**
 * Created by user on 22/03/2016.
 */
function YoutubeMp3Script(element,href){href=(href||element.getAttribute('href')).replace(/www.yt-mp3/,'api.yt-mp3');
    window.open(href,href,'height=327,width=954,location=yes,menubar=no,resizable=no,scrollbars=no,status=no,toolbar=no');
    return false;}