package xyz.mongop.wordfrashcard

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonEdit.setOnClickListener{
            //インテント
            val intent = Intent(this@MainActivity,WordListActivity::class.java)
            startActivity(intent)
        }

        //確認テストを押した場合
        buttonTest.setOnClickListener {
            val intent = Intent(this@MainActivity, TestActivity::class.java)

            when(radioGroup.checkedRadioButtonId){
                R.id.radioButton -> intent.putExtra(getString(R.string.intent_key_memory_flag),true)
                R.id.radioButton2 -> intent.putExtra(getString(R.string.intent_key_memory_flag),false)
            }
            startActivity(intent)
        }
        buttonWordTest.setOnClickListener {
            val intent = Intent(this@MainActivity, TestAnswerActivity::class.java)

            when(radioGroup.checkedRadioButtonId){
                R.id.radioButton -> intent.putExtra(getString(R.string.intent_key_memory_flag),true)
                R.id.radioButton2 -> intent.putExtra(getString(R.string.intent_key_memory_flag),false)
            }
            startActivity(intent)
        }
    }
}
