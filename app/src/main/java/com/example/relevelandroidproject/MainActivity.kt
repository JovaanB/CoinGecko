package com.example.relevelandroidproject

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.relevelandroidproject.domain.model.Coin
import com.example.relevelandroidproject.presentation.AfterNotification.AfterNotificationActivity
import com.example.relevelandroidproject.presentation.CoinList.CoinAdapter
import com.example.relevelandroidproject.presentation.CoinListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private var valueRepeat = 3
    private lateinit var coinRecyclerView   : RecyclerView
    private lateinit var coinAdapter        : CoinAdapter
    private lateinit var showNotificationBtn: Button
    private lateinit var progressBar        : ProgressBar
    private lateinit var search             : String
    private lateinit var layoutManager : GridLayoutManager
    private lateinit var sort : Button
    private val tempCoinList = arrayListOf<Coin>()
    private var page = 1
    private val coinListViewModel : CoinListViewModel by viewModels()

    private companion object {
        private const val CHANNEL_ID = "channel01"
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sort = findViewById(R.id.btSort)
        progressBar         = findViewById(R.id.progressBar)
        coinRecyclerView   = findViewById(R.id.coinRecyclerView)
        layoutManager = GridLayoutManager(this,2)
        recyclerView()
        sort.setOnClickListener{
            tempCoinList.sortWith { o1, o2 -> o1!!.name.compareTo(o2!!.name) }
            coinAdapter.setData(tempCoinList)
        }
        coinRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(layoutManager.findLastVisibleItemPosition()==layoutManager.itemCount-1)
                {
                    page += 1
                    coinListViewModel.getAllCoins(page.toString())
                    callAPI()
                }
            }
        })

        showNotificationBtn = findViewById(R.id.showNotificationBtn)

        showNotificationBtn.setOnClickListener {
            showNotification()
        }
    }

    private fun showNotification() {
        createNotificationChannel()

        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()

        val intent = Intent(this, AfterNotificationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK;
            putExtra("KEY_NAME", "Jovan");
            putExtra("KEY_EMAIL", "jovan.bienvenu@enigma-school.com");
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.setContentTitle("Notification Coin Gecko")
        notificationBuilder.setContentText("Click on me!")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(pendingIntent)

        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "MyNotification"
            val description = "My notification channel description"

            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = description
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun callAPI(){
        CoroutineScope(Dispatchers.Main).launch {
            repeat(valueRepeat){
                coinListViewModel._coinListValue.collect{value->
                    when {
                        value.isLoading -> {
                            progressBar.visibility = View.VISIBLE
                        }
                        value.error.isNotBlank() -> {
                            progressBar.visibility = View.GONE
                            valueRepeat = 0
                            Toast.makeText(this@MainActivity, value.error, Toast.LENGTH_LONG).show()
                        }
                        value.coinsList.isNotEmpty() -> {
                            progressBar.visibility = View.GONE
                            valueRepeat = 0
                            tempCoinList.addAll(value.coinsList)
                            coinAdapter.setData(tempCoinList as ArrayList<Coin>)
                        }
                    }
                    delay(1000)
                }
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val search = menu?.findItem(R.id.menuSearch)
        val searchView = search?.actionView as androidx.appcompat.widget.SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextSubmit(queryText: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText?.isEmpty()!!){
            coinAdapter.setData(tempCoinList)
        }
        else{
            coinAdapter.filter.filter(newText)
        }
        return true
    }

    private fun recyclerView(){
        coinAdapter = CoinAdapter(this@MainActivity,ArrayList())
        coinRecyclerView.adapter = coinAdapter
        coinRecyclerView.layoutManager = layoutManager
        coinRecyclerView.addItemDecoration(DividerItemDecoration(coinRecyclerView.context,(GridLayoutManager(this,1)).orientation))
    }

    override fun onStart() {
        super.onStart()
        coinListViewModel.getAllCoins(page.toString())
        callAPI()
    }
}