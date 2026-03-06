package moe.shizuku.manager.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import moe.shizuku.manager.databinding.HomeDiagnosticBinding
import moe.shizuku.manager.databinding.HomeItemContainerBinding
import moe.shizuku.manager.diagnostic.DiagnosticActivity
import rikka.recyclerview.BaseViewHolder
import rikka.recyclerview.BaseViewHolder.Creator

class DiagnosticViewHolder(binding: HomeDiagnosticBinding, root: View) : BaseViewHolder<Any?>(root) {

    companion object {
        val CREATOR = Creator<Any> { inflater: LayoutInflater, parent: ViewGroup? ->
            val outer = HomeItemContainerBinding.inflate(inflater, parent, false)
            val inner = HomeDiagnosticBinding.inflate(inflater, outer.root, true)
            DiagnosticViewHolder(inner, outer.root)
        }
    }

    init {
        root.setOnClickListener { v: View ->
            v.context.startActivity(Intent(v.context, DiagnosticActivity::class.java))
        }
    }
}
