package com.monied.budgetapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.monied.budgetapp.databinding.FragmentDashboardBinding
import com.monied.budgetapp.ui.main.ViewExpensesActivity

//import com.monied.budgetapp.ui.budget.BudgetGoalsActivity
//import com.monied.budgetapp.ui.savings.SavingsGoalsActivity
//import com.monied.budgetapp.ui.alerts.BudgetAlertsActivity
//import com.monied.budgetapp.ui.profile.UserProfileActivity

/**
 * Dashboard Fragment - Home screen showing budget overview and quick actions
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    // TODO: Add ViewModel
    // private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Initialize ViewModel
        // viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
       /*
        // Profile avatar click
        binding.ivProfileAvatar.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfileActivity::class.java))
        }

        // Budget Goals banner click
        binding.cardBudgetGoals.setOnClickListener {
            startActivity(Intent(requireContext(), BudgetGoalsActivity::class.java))
        }

        // Savings Goals card click
        binding.cardSavingsGoals.setOnClickListener {
            startActivity(Intent(requireContext(), SavingsGoalsActivity::class.java))
        }*/

        // Budget Alerts card click
        binding.cardBudgetAlerts.setOnClickListener {
            startActivity(Intent(requireContext(), ViewExpensesActivity::class.java))

        }

        // View All expenses click
        binding.tvViewAll.setOnClickListener {
            // Navigate to History fragment via MainActivity
            (activity as? com.monied.budgetapp.ui.main.MainActivity)?.let { mainActivity ->
                requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                    com.monied.budgetapp.R.id.bottomNavigation
                )?.selectedItemId = com.monied.budgetapp.R.id.navigation_history
            }
        }
    }

    private fun setupObservers() {
        // TODO: Observe LiveData from ViewModel
        // viewModel.currentMonthSpending.observe(viewLifecycleOwner) { spending ->
        //     binding.tvSpendingAmount.text = String.format("R %.2f", spending)
        // }

        // viewModel.budgetGoal.observe(viewLifecycleOwner) { goal ->
        //     goal?.let {
        //         binding.tvMinGoal.text = String.format("R %.0f", it.minimumGoal)
        //         binding.tvMaxGoal.text = String.format("R %.0f", it.maximumGoal)
        //         updateProgressBar(spending, it.minimumGoal, it.maximumGoal)
        //     }
        // }

        // viewModel.recentExpenses.observe(viewLifecycleOwner) { expenses ->
        //     // Update RecyclerView adapter
        // }

        // For now, set static data
        binding.tvSpendingAmount.text = "R 1,245.00"
        binding.tvMinGoal.text = "Min: R 500"
        binding.tvMaxGoal.text = "Max: R 2,000"
        binding.progressBudget.progress = 62
        binding.tvSavingsAmount.text = "R 21,300"
        binding.tvAlertCount.text = "3 New"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
