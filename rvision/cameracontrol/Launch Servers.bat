cd dist
start java -cp RVision.jar rvision.UdpCameraServer -s COM1 -p 4000
start java -cp RVision.jar rvision.HttpCameraServer 4000 4002

SET GROUP=225.0.0.1
SET RTP_PORT=6970
SET HTTP_PORT=6972
SET SDP_MULTI_FILE=c:\wamp\www\camera\multicast.sdp
SET VLC=C:\Program Files\VideoLAN\VLC\VLC.EXE
REM SET VDEV=ICatch (VI) PC Camera
SET VDEV=USB 2820 Device

rem SET SIZE=640x480
SET SIZE=320x240

REM
REM This one shows a preview window
REM
REM "%VLC%" dshow:// :dshow-vdev="%VDEV%" :dshow-adev="none" :dshow-size="%SIZE%" --sout #transcode{vcodec=mp4v,vb=512,scale=1,fps=10}:duplicate{dst=display,dst=std{access=http,mux=ts,dst=:%HTTP_PORT%},dst=rtp{dst=127.0.0.1,port=6974,sdp=file://c:\Movies\camera.sdp}}





REM
REM This one DOES NOT show a preview window
REM
 "%VLC%" dshow:// :dshow-vdev="%VDEV%" :dshow-adev="none" :dshow-size="%SIZE%" --sout #transcode{vcodec=mp4v,vb=512,scale=1,fps=10}:duplicate{dst=std{access=http,mux=ts,dst=:%HTTP_PORT%},dst=rtp{dst=127.0.0.1,port=6974,sdp=file://c:\Movies\camera.sdp}}


