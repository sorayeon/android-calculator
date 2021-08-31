package shop.sorayeon.android.calculator

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.room.Room
import shop.sorayeon.android.calculator.model.History
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    // 계산식 표현 TextView
    private val expressionTextView: TextView by lazy {
        findViewById<TextView>(R.id.expressionTextView)
    }

    // 계산결과 TextView
    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTextView)
    }

    // 계산결과 History 레아이웃
    private val historyLayout: View by lazy {
        findViewById<View>(R.id.historyLayout)
    }

    // 계산결과 History 레아이웃
    private val historyLinearLayout: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    // Database
    lateinit var db: AppDatabase

    // 마지막으로 누른 버튼이 연산자 버튼인지 상태
    private var isOperator = false

    // 연산자 포함여부
    private var hasOperator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 데이터베이스 빌드
        db = AppDatabase.getInstance(applicationContext)!!
//        db = Room.databaseBuilder(
//            applicationContext,
//            AppDatabase::class.java,
//            "historyDB"
//        ).build()
    }

    // 숫자 및 연산자 클릭 이벤트 함수
    // 이벤트는 layout xml 에서 연결함 (android:onClick="buttonClicked")
    fun buttonClicked(view: View) {
        // 버튼의 id 값이 숫자면 numberButtonClicked
        // 버튼의 id 값이 연산자라면 operatorButtonClicked
        when (view.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMulti -> operatorButtonClicked("*")
            R.id.buttonDivider -> operatorButtonClicked("/")
            R.id.buttonModulo -> operatorButtonClicked("%")
        }
    }

    // 숫자버튼 클릭 함수
    private fun numberButtonClicked(number: String) {

        // 마지막 클릭한 버튼이 연산자 버튼이라면 " " 한칸 띄워준다. (1 + 숫자)
        if (isOperator) {
            expressionTextView.append(" ")
        }

        // 숫자가 입력되었으므로 isOperator 함수를 false 로 변경한다.
        isOperator = false

        // 계산식 표현은 숫자" "+" "숫자 형태이므로. " " 로 나눠주면 [숫자, 연산자, 숫자] 배열의 형태로 반환된다.
        val expressionText = expressionTextView.text.split(" ")

        // 유효성 검증
        // 최대 15자리 숫자만 계산가능 (입력된 값이 있으면서 마지막 배열의 값의 length 가 15보다 크거나 같으면 입력불가)
        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15자리 까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        // 첫 숫자는 0이 올수 없다 (마지막 배열값이 비워있는데. 선택한 버튼이 0 이 올수 없다)
        } else if (expressionText.last().isEmpty() && number == "0") {
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 계산식 표현에 선택한 숫자를 붙인다.
        expressionTextView.append(number)

        // resultTextView 에  실시간으로 계산 결과를 넣는 기능
        resultTextView.text = calculateExpression()
    }

    // 연산자 (+, -, *, /, %) 버튼 클릭함수
    private fun operatorButtonClicked(operator: String) {

        // 유효성 검증
        // 처음 선택한 버튼이 연산자일수 없다. (숫자입력없이 연산자 먼저 클릭할 수 없다)
        if (expressionTextView.text.isEmpty()) {
            return
        }

        when {
            // 연산자를 연속해서 두번 누른경우. 기존연산자는 삭제하고 새로운 연산자를 붙여준다.
            isOperator -> {
                val text = expressionTextView.text.toString()
                expressionTextView.text = text.dropLast(1) + operator
            }
            // 이미 연산자를 가지고 있는경우. 예외처리
            hasOperator -> {
                Toast.makeText(this, "연산자는 한 번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }
            // 정상적으로 숫자 입력뒤 연산자를 클릭한경우 " " + 연산자를 붙여준다.
            else -> {
                expressionTextView.append(" $operator")
            }
        }

        // TextView 의 문자 스타일을 변경(연산자를 초록색으로)하기 위해 SpannableStringBuilder 사용함
        val ssb = SpannableStringBuilder(expressionTextView.text)
        ssb.setSpan(
            ForegroundColorSpan(getColor(R.color.green)),
            expressionTextView.text.length - 1,
            expressionTextView.text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 스타일이 적용된 Spannable 문자를 계산식 표현에 넣는다.
        expressionTextView.text = ssb

        // 마지막입력값 연산자여부 true
        isOperator = true
        // 연산자 포함여부 true
        hasOperator = true
    }

    // 계산식 표현을 실제계산하는 함수
    private fun calculateExpression(): String {
        // 계산식 표현을 " " 로 분할 [숫자, 연산자, 숫자] 배열의 형태로 반환
        val expressionTexts = expressionTextView.text.split(" ")

        // 유효성 검증
        // 연산자가 포함되지 않거나 배열의 size 가 3이 아니라면 계산 불가
        if (hasOperator.not() || expressionTexts.size != 3) {
            return ""
        // 배열[0], 배열[2] 이 숫자가 아니라면 계산 불가
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }

        // 첫번째 숫자
        val exp1 = expressionTexts[0].toBigInteger()
        // 두번째 숫자
        val exp2 = expressionTexts[2].toBigInteger()
        // 연산자
        val op = expressionTexts[1]

        // 연산자에 따라 적절한 연산을 수행한 결과를 String 으로 반환
        return when (op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "*" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    // 클리어 (C) 버튼 클릭 (전체 초기화)
    fun clearButtonClicked(view: View) {
        // 계산결과 내용 초기화
        resultTextView.text = ""
        // 계산식 표현 초기화
        expressionTextView.text = ""
        // 마지막 클릭버튼 연산자 아님
        isOperator = false
        // 연산자 포함안함
        hasOperator = false
    }

    // 계산결과 (=) 버튼 클릭
    fun resultButtonClicked(view: View) {
        // 계산식 표현을 " " 로 분할 [숫자, 연산자, 숫자] 배열의 형태로 반환
        val expressionTexts = expressionTextView.text.split(" ")

        // 계산식 표현이 비워 있거나 배열의 사이즈가 1 이라면 계산 불가
        if (expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        // 배열의 size 가 3이 아니면서 연산자를 가지고 있다면 완성되지 않은 수식
        if (expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "아직 완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 배열[0], 배열[2] 이 숫자가 아니라면 오류 발생 (사실 오류를 내는것이 불가능)
        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 계산식 표현을 계산한 문자열
        val expressionText = expressionTextView.text.toString()
        val resultText = calculateExpression()

        // 계산결과를 DB 에 이력으로 저장 - 등록
        Thread(Runnable {
            db.historyDao().insertHistory(History(expressionText, resultText))
        }).start()

        // 계산결과 내용 초기화
        resultTextView.text = ""
        // 계산식 표현을 계산결과로 세팅
        expressionTextView.text = resultText

        // 마지막 클릭버튼 연산자 아님
        isOperator = false
        // 연산자 포함안함
        hasOperator = false
    }

    // 계산기록 이력보기 버튼 클릭
    fun historyButtonClicked(view: View) {
        // 계산기록 이력보기 레이어 노출
        historyLayout.isVisible = true

        // 뷰에서 모든 기록 삭제 (DB 에서 새로 가져옴)
        historyLinearLayout.removeAllViews()

        // DB 에 저장된 계산 이력으로 전체 - 조회 -> 뷰에 표시
        Thread(Runnable {
            // 디비에서 모든 기록 가져오기
            db.historyDao().getAll().reversed().forEach {
                // 메인 레이아웃의 내용을 수정하려면 UI Thread 를 열어준다.
                runOnUiThread {
                    // 레이아웃 XML 파일을 View 객체로 만들기 위해 LayoutInflater 를 이용
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"
                    // root 설정 없이 직접 레이아웃에 View 를 붙여준다.
                    historyLinearLayout.addView(historyView)
                }
            }
        }).start()
    }

    // 닫기버튼 클릭 이벤트
    fun closeHistoryButtonClicked(view: View) {
        // 계산기록 이력 레이아웃 숨기기
        historyLayout.isVisible = false
    }

    // 계산 기록 삭제 버튼 클릭 이벤트
    fun historyClearButtonClicked(view: View) {
        // 뷰에서 모든 기록 삭제
        historyLinearLayout.removeAllViews()

        // DB 에 저장된 계산 이력으로 전체 - 삭제
        Thread(Runnable {
            // 디비에서 모든 기록 삭제
            db.historyDao().deleteAll()
        }).start()
    }
}

// 확장함수 생성
fun String.isNumber(): Boolean {
    return try {
        // 15자리 숫자까지 가능하기때문에 BigInteger 로 변환해보고 성공하면 true 반환
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        // 변환이 실패(NumberFormatException)하면 false 반환
        false
    }
}