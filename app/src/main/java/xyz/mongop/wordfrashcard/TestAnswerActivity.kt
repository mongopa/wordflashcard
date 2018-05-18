package xyz.mongop.wordfrashcard

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*

//問題と答えの表示を逆にしたテスト。
//相違点は139行目と152行目のみ。
class TestAnswerActivity : AppCompatActivity(), View.OnClickListener {
    //テスト条件（暗記済みの単語を除外するか：する＝＞true）
    var boolStatusMemory: Boolean = false
    //問題を暗記済みにするかどうか
    var boolMemorized: Boolean = false

    //テストの状態
    var intState: Int = 0
    val BEFORE_START: Int = 1       //テスト開始前
    val RUNNING_QUESTION: Int = 2   //問題を出した段階
    val RUNNING_ANSWER: Int = 3     //こたえを出した段階
    val TEST_FINISHED: Int = 4      //テスト終了

    //realm関係
    lateinit var realm: Realm
    lateinit var results: RealmResults<WordDB>
    lateinit var word_list: ArrayList<WordDB>

    var intLength:Int = 0   //レコードの数(テストの問題数）
    var intCount:Int = 0    //今何問めを示すカウンター

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        // インテント受け取り
        val bundle = intent.extras
        boolStatusMemory = bundle.getBoolean(getString(R.string.intent_key_memory_flag))

        //テスト状態を「開始前」に+カード画像非表示
        intState = BEFORE_START
        imageViewFlashQuestion.visibility = View.INVISIBLE
        imageViewFlashAnswer.visibility = View.INVISIBLE


            //ボタン①をテストをはじめる
        buttonNext.setText(R.string.button_start)

        //ボタン②をやめる
        buttonEndTest.setText(R.string.button_test_end)

        //クリックリスナー
        buttonNext.setOnClickListener(this)
        buttonEndTest.setOnClickListener(this)
        checkBox.setOnClickListener {
            //暗記済みのチェックボックスを押したときは暗記済みのステータスを変える
            boolMemorized = checkBox.isChecked
        }
    }
    override fun onClick(p0: View?) {
        when(p0!!.id){
        // 右のボタンを押したとき
            R.id.buttonNext ->
                when(intState){
                    BEFORE_START -> {
                        //「テスト開始前」の場合
                        //「問題を出した段階」に+問題表示(showQuestion)
                        intState = RUNNING_QUESTION
                        showQuestion()
                    }


                    RUNNING_QUESTION -> {
                        //「問題を出した段階」の場合
                        //「こたえを出した段階」に+答え表示(showAnswer)
                        intState = RUNNING_ANSWER
                        showAnswer()
                    }


                    RUNNING_ANSWER -> {
                        // 「こたえを出した段階」の場合
                        //「問題を出した段階」に+問題表示(showQuestion)
                        intState = RUNNING_QUESTION
                        showQuestion()
                    }


                }

            R.id.buttonEndTest -> {
                // もどるボタンを押した場合
                val dialog = AlertDialog.Builder(this@TestAnswerActivity).apply {
                    setTitle("テストの終了")
                    setMessage("テストを終了してもいいですか？")
                    setPositiveButton("はい"){ dialog, which ->
                        //「テスト終了」の場合、最後の問題の暗記済フラグをDBに登録(更新)
                        if (intState == TEST_FINISHED){
                            val selectedDB = realm.where(WordDB::class.java).equalTo(getString(R.string.db_field_question)
                                    , word_list[intCount - 1].strQuestion).findFirst()
                            realm.beginTransaction()
                            selectedDB?.boolMemoryFlag = boolMemorized
                            realm.commitTransaction()
                        }

                        finish()
                    }
                    setNegativeButton("いいえ"){dialog, which ->  }
                    show()

                }
            }
        }
    }
    private fun showQuestion() {
        // 問題表示処理(showQuestionメソッド)
        // 前の問題の暗記済フラグをDB登録（更新）
        if (intCount > 0){
            //２問目以降のときのみ発生
            val selectedDB = realm.where(WordDB::class.java).equalTo(getString(R.string.db_field_question),
                    word_list[intCount - 1].strQuestion).findFirst()
            realm.beginTransaction()
            selectedDB?.boolMemoryFlag = boolMemorized
            realm.commitTransaction()
        }


        // のこり問題数を１つ減らして表示
        intCount ++
        textViewRemaining.text = (intLength - intCount).toString()



        // 今回の問題表示・前のこたえ消去（画像と文字）
        imageViewFlashAnswer.visibility = View.INVISIBLE
        textViewFlashAnswer.text = ""
        imageViewFlashQuestion.visibility = View.VISIBLE
        textViewFlashQuestion.text = word_list[intCount - 1].strAnswer

        // ボタン①を「こたえを見る」に
        buttonNext.setText(R.string.button_answer)

        // 問題の単語が暗記済の場合はチェックを入れる
        checkBox.isChecked = word_list[intCount - 1].boolMemoryFlag
        boolMemorized = checkBox.isChecked
    }
    private fun showAnswer() {
        // こたえ表示処理(showAnswerメソッド)
        // こたえの表示（画像・文字）
        imageViewFlashAnswer.visibility = View.VISIBLE
        textViewFlashAnswer.text = word_list[intCount - 1].strQuestion

        // ボタン①を「次の問題にすすむ」に
        buttonNext.setText(R.string.button_next)

        // 最後の問題まで来たら
        if (intLength == intCount){
            // テスト状態を「終了」にしてメッセージ表示
            intState = TEST_FINISHED
            textViewMessage.text = "テスト終了"

            //ボタン①を見えない＆使えないように
            buttonNext.isEnabled = false
            buttonNext.visibility = View.INVISIBLE

            // ボタン②を「もどる」に
            buttonEndTest.setText(R.string.button_back)
        }

    }
    override fun onResume() {
        super.onResume()

        //realmインスタンスの取得
        realm = Realm.getDefaultInstance()

        //DBからテストデータ取得(テスト条件で処理分岐)
        if (boolStatusMemory){
            //暗記済みの単語を除外する
            results = realm.where(WordDB::class.java).equalTo(getString(R.string.db_field_memory_flag), false).findAll()
        } else {
            //暗記済みの単語を除外しない（含める）
            results = realm.where(WordDB::class.java).findAll()
        }

        //残り問題数の表示
        intLength = results.size
        textViewRemaining.text = intLength.toString()

        if(intLength == 0){
            Toast.makeText(this@TestAnswerActivity, "単語がありません", Toast.LENGTH_SHORT).show()
            buttonNext.isEnabled = false
        }

        //取得したテストデータをシャッフル
        word_list = ArrayList(results)
        Collections.shuffle(word_list)
    }

    override fun onPause() {
        super.onPause()

        //realmの後片付け
        realm.close()
    }
}
