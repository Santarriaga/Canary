package com.grumpy.canary.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.grumpy.canary.ContactsApplication
import com.grumpy.canary.R
import com.grumpy.canary.adapters.ContactListAdapter
import com.grumpy.canary.databinding.FragmentContactsListBinding
import com.grumpy.canary.viewmodels.ContactsViewModel
import com.grumpy.canary.viewmodels.ContactsViewModelFactory


class ContactsListFragment : Fragment() {

    //set up binding
    private var _binding : FragmentContactsListBinding ?= null
    private val binding get() = _binding!!

    //declare views
    private lateinit var recyclerView: RecyclerView

    //reference to viewModel
    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactsDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentContactsListBinding.inflate(inflater, container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //adapter property
        val adapter = ContactListAdapter{
            val action = ContactsListFragmentDirections.actionContactsFragmentToAddContactFragment(it.id)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter

        viewModel.allContacts.observe(this.viewLifecycleOwner){contacts ->
            contacts.let {
                adapter.submitList(it)
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_contactsFragment_to_addContactFragment)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}