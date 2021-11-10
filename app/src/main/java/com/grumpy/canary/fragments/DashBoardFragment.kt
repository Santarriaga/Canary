package com.grumpy.canary.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.grumpy.canary.ContactsApplication
import com.grumpy.canary.R
import com.grumpy.canary.database.Contacts
import com.grumpy.canary.databinding.FragmentDashBoardBinding
import com.grumpy.canary.viewmodels.ContactsViewModel
import com.grumpy.canary.viewmodels.ContactsViewModelFactory

class DashBoardFragment : Fragment() {

    private var _binding : FragmentDashBoardBinding?= null
    private val  binding get() = _binding!!

    private lateinit var contact: Contacts

    //viewModel reference
    private val viewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(
            (activity?.application as ContactsApplication).database.contactsDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDashBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateViews()

    }

    private fun initiateViews(){

        viewModel.allContacts.observe(this.viewLifecycleOwner){ myList ->
            if(myList.isNotEmpty()){
               contact = myList[0]
               bind(contact)
                binding.user0.text = myList.elementAtOrNull(0)?.name
                binding.user1.text = myList.elementAtOrNull(1)?.name
                binding.user2.text = myList.elementAtOrNull(2)?.name

            }else{
                binding.messageDisplayed.text = getString(R.string.default_message)
            }

        }
    }

    private fun bind (contact : Contacts){
        binding.messageDisplayed.text = contact.message
        binding.customizeBtn.setOnClickListener {
            val action = DashBoardFragmentDirections.actionDashBoardFragmentToAddContactFragment(contact.id)
            findNavController().navigate(action)
        }
    }
}
