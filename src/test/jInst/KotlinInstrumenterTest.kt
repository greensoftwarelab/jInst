package jInst

import com.github.javaparser.JavaParser
import jInst.Instrumentation.InstrumentHelper
import jInst.Instrumentation.KotlinInstrumenter
import jInst.Instrumentation.Utils.KtlTestsInstrumenterUtil
import jInst.Instrumentation.readFileToString
import jdk.internal.util.xml.impl.Input
import kastree.ast.Writer
import kastree.ast.psi.Converter
import org.junit.jupiter.api.Assertions.*
import kastree.ast.psi.Parser;
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException
import org.junit.jupiter.api.Test
import java.io.*


internal class KotlinInstrumenterTest {

    val libPrefix : String = "Trepn"

    @org.junit.jupiter.api.Test
    fun validReturnReplacement() {
        val originalcode = """
        fun insertInBegin( node: List<Node.Stmt>, stm : Node.Expr , stmPrefix: String): List<Node.Stmt> {
            return if ( Writer.write(node.first()).startsWith( stmPrefix )){
                        node
                    }
                    else{
                    (  listOf( Node.Stmt.Expr(stm) ) + node)
                    }
                 }
        """.trimIndent()
        val expectedCode = """
        import com.greenlab.trepnlib.TrepnLib
        fun insertInBegin(node: List<Node.Stmt>, stm: Node.Expr, stmPrefix: String): List<Node.Stmt> {
        TrepnLib.updateState(null, 1, "insertInBegin")
        var myjInstRetVar = if (Writer.write(node.first()).startsWith(stmPrefix)) {
            node
        } else {
            (listOf(Node.Stmt.Expr(stm)) + node)
        }
        TrepnLib.updateState(null, 0, "insertInBegin")
        return myjInstRetVar
    }""".trimIndent()
        val expected = Parser.parseFile(expectedCode)
        val file = Parser.parseFile(originalcode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.METHOD, JavaParser.parseExpression("null"))
        val newFile = kt.instrument(file)
        assertEquals(Writer.write(expected), Writer.write(newFile))
    }

    @org.junit.jupiter.api.Test
    fun validMethodOrientedInstrumentation() {
        val  inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "original.txt" );
        val originalcode = inputStream.readBytes().toString(Charsets.UTF_8).trimIndent()
        val  inputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "expectedMethodOriented.txt" );
        val expectedCode = inputStream2.readBytes().toString(Charsets.UTF_8).trimIndent()
        val expected = Parser.parseFile(expectedCode)
        val file = Parser.parseFile(originalcode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.METHOD, JavaParser.parseExpression("null"))
        val newFile = kt.instrument(file)
        assertEquals(Writer.write(expected), Writer.write(newFile))
    }

    @org.junit.jupiter.api.Test
    fun validTestOrientedInstrumentation() {
        val  inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "original.txt" );
        val originalcode = inputStream.readBytes().toString(Charsets.UTF_8).trimIndent()
        val  inputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "expectedTestOriented.txt" );
        val expectedCode = inputStream2.readBytes().toString(Charsets.UTF_8).trimIndent()
        val expected = Parser.parseFile(expectedCode)
        val file = Parser.parseFile(originalcode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.TEST, JavaParser.parseExpression("null"))
        val newFile = kt.instrument(file)
        assertEquals(Writer.write(expected), Writer.write(newFile))
    }

    @org.junit.jupiter.api.Test
    fun validProfilerCalls() {
        val ctx = "null"
        // TEST
        var expectedCall = "TrepnLib.startProfilingTest(${ctx})"
        var kt = KotlinInstrumenter(JInst.InstrumentationType.TEST, JavaParser.parseExpression(ctx))
        assertEquals( expectedCall,Writer.write(kt.getAppropriateTestCall(true)))

        expectedCall = "TrepnLib.stopProfilingTest(${ctx})"
        kt = KotlinInstrumenter(JInst.InstrumentationType.TEST, JavaParser.parseExpression(ctx))
        assertEquals(expectedCall, Writer.write(kt.getAppropriateTestCall(false)))
        // Method
        expectedCall = "TrepnLib.startProfiling(${ctx})"
        kt = KotlinInstrumenter(JInst.InstrumentationType.METHOD, JavaParser.parseExpression(ctx))
        assertEquals( expectedCall,Writer.write(kt.getAppropriateTestCall(true)))

        expectedCall = "TrepnLib.stopProfiling(${ctx})"
        kt = KotlinInstrumenter(JInst.InstrumentationType.METHOD, JavaParser.parseExpression(ctx))
        assertEquals(expectedCall, Writer.write(kt.getAppropriateTestCall(false)))
    }

    @org.junit.jupiter.api.Test
    fun checkIfIsKotlinTest() {
        val pathToAndroidKtTest = "/Users/ruirua/tests/ktlnTest/org.ligi.scr/android/src/main/java/org/ligi/scr/MainActivity.kt" // "simpleAndroidTest.txt"

       // val  inputStream2 = // Thread.currentThread().getContextClassLoader().getResourceAsStream( pathToAndroidKtTest );
        val testcode =  readFileToString(pathToAndroidKtTest)
        val codeRepr = Parser.parseFile(testcode)
        assertTrue(   ! KtlTestsInstrumenterUtil.isTestFile(codeRepr) )
    }


    @org.junit.jupiter.api.Test
    fun validTestClassInstrumentation() {
        val pathToAndroidKtTest = "simpleAndroidTest.txt"
        val expectedCode = """package paco

import com.jraska.falcon.FalconSpoon
import org.junit.Rule
import org.junit.Test
import org.ligi.scr.MainActivity
import org.ligi.trulesk.TruleskActivityRule
import com.greenlab.trepnlib.TrepnLib

class TheMainActivity {
    @get:Rule
    var rule = TruleskActivityRule(MainActivity::class.java)

    @Test
    fun testMainActivityStarts() {
        TrepnLib.traceTest("paco.${"$"}TheMainActivity${"$"}testMainActivityStarts")
        FalconSpoon.screenshot(rule.activity, "main")
    }

    @AfterEach
    fun anaDroidAfterEach() {
        TrepnLib.stopProfilingTest(null)
    }

    @BeforeEach
    fun anaDroidBeforeEach() {
        TrepnLib.startProfilingTest(null)
    }
}""".trimIndent()
        val  inputStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( pathToAndroidKtTest );
        val testcode = inputStream2.readBytes().toString(Charsets.UTF_8).trimIndent()
        val codeRepr = Parser.parseFile(testcode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.TEST, JavaParser.parseExpression("null"))
        InstrumentHelper.compiledSdkVersion=24
        val newFile = kt.instrumentTestFile(codeRepr)
      //  println(Writer.write(newFile))
//        assertEquals(expectedCode ,Writer.write(newFile).trimIndent())  //TODO
        assertTrue(true)
    }

    @org.junit.jupiter.api.Test
    fun XtestFake() {
        //val file = ("/Users/ruirua/Downloads/datada.json")

        val file = ("allMethods.json")
        var jsonObject = JSONObject()
        try {
            val obj = JSONParser().parse(FileReader(file))
            jsonObject = obj as JSONObject
        } catch (var6: FileNotFoundException) {
            var6.printStackTrace()
        } catch (var7: IOException) {
            var7.printStackTrace()
        } catch (var8: ParseException) {
            var8.printStackTrace()
        }

    }

/*
    @org.junit.jupiter.api.Test
    fun launchActivityTest1() {
        val inCode = """
package uminho.di.greenlab.kotlinsimpleapp;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}

""".trimIndent()
        val extrasMap = Converter.WithExtras()
        val file = Parser(extrasMap).parseFile(code)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.ACTIVITY, JavaParser.parseExpression("null"))
        val out = kt.instrument(codeRepr)
        Writer.write(out)
    }
*/
    @org.junit.jupiter.api.Test
    fun launchActivityTestWithOnCreateAndDestroy() {
   val inCode = """
package com.ruirua.futexam.ui.activities

import android.content.Intent
import android.os.Bundle


class ItemListActivity : AppCompatActivity() {
    private var mTwoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
       
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        setSupportActionBar(toolbar)
        toolbar.title = title
        setupRecyclerView(item_list)
    }
    
     override fun onDestroy() : Unit {
        super.onDestroy()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, SampleData.getSampleCategories(), mTwoPane)
    }

    class SimpleItemRecyclerViewAdapter(private  val mParentActivity: ItemListActivity, private  val mValues: List<Category>, private  val mTwoPane: Boolean) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        private val mOnClickListener: View.OnClickListener
        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                if (mTwoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.category_id.toString())
                        }
                    }
                    mParentActivity.supportFragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.category_id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.category_id.toString()
            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.cat_name_view

            val mContentView: FloatingActionButton = mView.fab
        }
    }
}""".trimIndent()
        val codeRepr = Parser.parseFile(inCode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.ACTIVITY, JavaParser.parseExpression("null"))
        val out = kt.instrument(codeRepr)
        val expectedResult="""package com.ruirua.futexam.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import java.io.File

class ItemListActivity : AppCompatActivity() {
    private var mTwoPane: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Debug.startMethodTracing(File("/sdcard", "anadroidDebugTrace.trace").getPath())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        setSupportActionBar(toolbar)
        toolbar.title = title
        setupRecyclerView(item_list)
    }

    override fun onDestroy(): Unit {
        super.onDestroy()
        Debug.stopMethodTracing()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, SampleData.getSampleCategories(), mTwoPane)
    }

    class SimpleItemRecyclerViewAdapter(private  val mParentActivity: ItemListActivity, private  val mValues: List<Category>, private  val mTwoPane: Boolean) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        private val mOnClickListener: View.OnClickListener

        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                if (mTwoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.category_id.toString())
                        }
                    }
                    mParentActivity.supportFragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.category_id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.category_id.toString()
            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.cat_name_view

            val mContentView: FloatingActionButton = mView.fab
        }
    }
}""".trimIndent()
        assertEquals( Writer.write(out) ,Writer.write( Parser.parseFile(expectedResult))  )

    }
    @org.junit.jupiter.api.Test
    fun instrumentlaunchActivityTestWithoutOnCreateAndDestroy() {
        val inCode = """
package com.ruirua.futexam.ui.activities

import android.content.Intent
import android.os.Bundle


class ItemListActivity : AppCompatActivity() {
    private var mTwoPane: Boolean = false
    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, SampleData.getSampleCategories(), mTwoPane)
    }

    class SimpleItemRecyclerViewAdapter(private  val mParentActivity: ItemListActivity, private  val mValues: List<Category>, private  val mTwoPane: Boolean) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        private val mOnClickListener: View.OnClickListener
        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                if (mTwoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.category_id.toString())
                        }
                    }
                    mParentActivity.supportFragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.category_id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.category_id.toString()
            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.cat_name_view

            val mContentView: FloatingActionButton = mView.fab
        }
    }
}""".trimIndent()
        val codeRepr = Parser.parseFile(inCode)
        val kt = KotlinInstrumenter(JInst.InstrumentationType.ACTIVITY, JavaParser.parseExpression("null"))
        val out = kt.instrument(codeRepr)
        val expectedResult="""package com.ruirua.futexam.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Debug
import java.io.File

class ItemListActivity : AppCompatActivity() {
    private var mTwoPane: Boolean = false

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, SampleData.getSampleCategories(), mTwoPane)
    }

    class SimpleItemRecyclerViewAdapter(private  val mParentActivity: ItemListActivity, private  val mValues: List<Category>, private  val mTwoPane: Boolean) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {
        private val mOnClickListener: View.OnClickListener

        init {
            mOnClickListener = View.OnClickListener { v ->
                val item = v.tag as Category
                if (mTwoPane) {
                    val fragment = ItemDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(ItemDetailFragment.ARG_ITEM_ID, item.category_id.toString())
                        }
                    }
                    mParentActivity.supportFragmentManager.beginTransaction().replace(R.id.item_detail_container, fragment).commit()
                } else {
                    val intent = Intent(v.context, ItemDetailActivity::class.java).apply {
                        putExtra(ItemDetailFragment.ARG_ITEM_ID, item.category_id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mValues[position]
            holder.mIdView.text = item.category_id.toString()
            with(holder.itemView) {
                tag = item
                setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return mValues.size
        }

        inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.cat_name_view

            val mContentView: FloatingActionButton = mView.fab
        }
    }

    override fun onCreate(var bd: Bundle?) {
        Debug.startMethodTracing(File("/sdcard", "anadroidDebugTrace.trace").getPath())
        super.onCreate(bd)
    }

    override fun onDestroy() {
        super.onDestroy(bd)
        Debug.stopMethodTracing()
    }
}""".trimIndent()
        assertEquals( Writer.write(out) ,Writer.write( Parser.parseFile(expectedResult))  )

    }

}