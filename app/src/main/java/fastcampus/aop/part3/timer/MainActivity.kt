package fastcampus.aop.part3.timer

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.SeekBar
import android.widget.TextView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private val remainMinutesTextView: TextView by lazy {
        findViewById(R.id.remainMinutesTextView)
    }

    private val remainSecondsTextView: TextView by lazy {
        findViewById(R.id.remainSecondsTextView)
    }

    private val seekBar: SeekBar by lazy {
        findViewById(R.id.seekBar)
    }
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null

    private val soundPool = SoundPool.Builder().build()

    private var currentCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() { //다시 돌아왔을 경우
        super.onResume()
        soundPool.autoResume() //모든 활성화된 스트림을 resume시킨다.

    }


    override fun onPause() { //앱이 화면에서 보이지 않을 경우
        super.onPause()
        //soundPool.pause(재생되고있는 스트림id값을 받으면, 그 id값에 해당되는 사운드만 pause한다.)
        soundPool.autoPause()//모든 활성화된 스트림을 전부 pause한다.

    }

    override fun onDestroy() { //더이상 이 앱을 사용하지 않을 때,
        super.onDestroy()
        soundPool.release() //사운드풀에 로드되었던 사운드 파일들을 메모리에서 해제한다.
    }


    private fun bindViews() {
        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener { //object : 클래스 선언과 동시에 객체 생성
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) { //progress=분을 나타낸다.
                    if(fromUser) { //코드상 변경이 아니라, 사용자가 건드렸을 때만 업데이트를 해준다.
                        updateRemainTimes(progress * 60 * 1000L)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) { //새로운 조작이 일어날 경우(다시 타이머를 셋업할 경우)
                    stopCountDown()

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) { //손가락을 떼는 순간(터치가 끝나는 순간)
                    seekBar ?: return //null일 경우에는 카운트 다운을 진행하지 않는다.
                    // ?: = 좌측에 있는 값이 null일 경우, 우측에 있는 값을 return 한다.
                    // 코틀린은 expression도 리턴을 할 수 있다. 따라서  null이 일어나면 return문을 바로 전달하여 onStopTrackingTouch를 리턴해버린다.

                    if(seekBar.progress==0) {
                        stopCountDown()
                    }else {
                        startCountDown() //seekBar가 null이 아닐 경우에만, 카운트다운을 시작한다.
                    }
                }

            }
        )
    }

    private fun initSounds() { //SoundPool : 오디오 사운드를 재생하고 관리하는 클래스
        //오디오 파일을 메모리에 로드한 다음 빠르게 재생할 수 있게 해준다.

        tickingSoundId = soundPool.load(this,R.raw.timer_ticking,1)
        bellSoundId = soundPool.load(this,R.raw.timer_bell,1)

    }

    private fun createCountDownTimer(initialMillis: Long) = //코틀린 타입으로 return 제거하는 표현식 가능
        object: CountDownTimer(initialMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) { //onTick는 1초마다 한번씩 불린다.
                updateRemainTimes(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            } //1초마다 ui를 갱신한다.

            override fun onFinish() { //카운트다운이 끝났을 때
                completeCountDown()
            }
        }

    private fun startCountDown() {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        currentCountDownTimer?.start()
        tickingSoundId?.let { soundId -> //널이 아닐 경우에만 이 메소드를 호출한다.
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F) //타이머가 시작되면 초침소리를 째깍째깍 loop시켜 넣는다.
        }

    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel() //널이 아닐 경우 cancle(카운트다운을 멈춘다)
        currentCountDownTimer = null // 카운트다운을 null로 만들어준다
        soundPool.autoPause() //ticking 사운드도 멈춰준다.
    }

    private fun completeCountDown() { //카운트다운이 끝났을 때 함수
        updateRemainTimes(0)
        updateSeekBar(0) //remainTime, seekBar를 0으로 셋업한다.

        soundPool.autoPause() //초침음을 멈춘다.

        bellSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, 0, 1F) //끝나는 종료음은 반복 재생하지 않는다.
        }

    }

    @SuppressLint("SetTextI18n")
    private fun updateRemainTimes(remainMills: Long) { //초를 받아서
        val remainSeconds = remainMills / 1000 // seconds값을 만든다
        remainMinutesTextView.text = "%02d'".format(remainSeconds / 60)
        remainSecondsTextView.text = "%02d".format(remainSeconds % 60) // 남은 seconds값

    }

    private fun updateSeekBar(remainMills: Long) { //받는 값의 단위를 통일시켜 놓는것이 가독성이 좋다.
        seekBar.progress = (remainMills / 1000 / 60).toInt()//분을 만든다.
    }


    }



