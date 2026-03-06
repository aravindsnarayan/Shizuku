package moe.shizuku.manager.management

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.snackbar.Snackbar
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppBarActivity
import moe.shizuku.manager.app.SnackbarHelper
import moe.shizuku.manager.databinding.AppsActivityBinding
import moe.shizuku.manager.utils.ShizukuStateMachine
import rikka.lifecycle.Status
import rikka.recyclerview.addEdgeSpacing
import rikka.recyclerview.fixEdgeEffect

class ApplicationManagementActivity : AppBarActivity() {

    private val viewModel: AppsViewModel by viewModels()
    private val adapter = AppsAdapter()

    private val stateListener: (ShizukuStateMachine.State) -> Unit = {
        if (ShizukuStateMachine.isDead() && !isFinishing)
            finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!ShizukuStateMachine.isRunning()) {
            finish()
            return
        }

        val binding = AppsActivityBinding.inflate(layoutInflater, rootView, true)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel.packages.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    adapter.updateData(it.data)
                }
                Status.ERROR -> {
                    val tr = it.error
                    Log.e("ApplicationManagement", "Failed to load app list", tr)
                    SnackbarHelper.show(
                        this,
                        binding.root,
                        msg = getString(R.string.app_management_load_error),
                        duration = Snackbar.LENGTH_INDEFINITE,
                        actionText = getString(android.R.string.ok),
                        action = { finish() },
                        onDismiss = { finish() }
                    )
                }
                Status.LOADING -> {

                }
            }
        }
        viewModel.load()

        val recyclerView = binding.list
        recyclerView.adapter = adapter
        recyclerView.fixEdgeEffect()
        recyclerView.addEdgeSpacing(top = 8f, bottom = 8f, unit = TypedValue.COMPLEX_UNIT_DIP)

        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                viewModel.load(true)
            }
        })

        ShizukuStateMachine.addListener(stateListener)
    }

    override fun onDestroy() {
        ShizukuStateMachine.removeListener(stateListener)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }
}
