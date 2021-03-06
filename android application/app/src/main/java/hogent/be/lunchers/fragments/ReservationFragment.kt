package hogent.be.lunchers.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import hogent.be.lunchers.R
import hogent.be.lunchers.activities.MainActivity
import hogent.be.lunchers.databinding.FragmentReservationBinding
import hogent.be.lunchers.utils.GuiUtil
import hogent.be.lunchers.utils.MessageUtil
import hogent.be.lunchers.viewmodels.LunchViewModel
import hogent.be.lunchers.viewmodels.ReservationViewModel
import kotlinx.android.synthetic.main.fragment_reservation.*
import java.util.*

/**
 * Een [Fragment] voor het plaatsen van een reservatie
 */
class ReservationFragment : Fragment() {

    /**
     * [ReservationViewModel] met de data over een reservatie plaatsen.
     */
    private lateinit var reservationViewModel: ReservationViewModel

    /**
     * [LunchViewModel] met de data over account
     */
    //Globaal ter beschikking gesteld aangezien het mogeiljks later nog in andere functie dan onCreateView wenst te worden
    private lateinit var lunchViewModel: LunchViewModel

    /**
     * De [FragmentReservationBinding] dat we gebruiken voor de effeciteve databinding.
     */
    private lateinit var binding: FragmentReservationBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reservation, container, false)

        //viewmodel vullen
        lunchViewModel = ViewModelProviders.of(requireActivity()).get(LunchViewModel::class.java)
        reservationViewModel = ViewModelProviders.of(requireActivity()).get(ReservationViewModel::class.java)

        //get selected selectedLunch from lunchvm
        reservationViewModel.setSelectedLunch(lunchViewModel.selectedLunch.value!!)

        val rootView = binding.root

        //databinding
        binding.reservationViewModel = reservationViewModel
        binding.setLifecycleOwner(activity)

        return rootView
    }

    /**
     * Instantieer de listeners
     */
    private fun initListeners() {
        //reservatie voltooid
        reservationViewModel.reservationPlaced.observe(this, Observer {
            if (reservationViewModel.reservationPlaced.value == true) {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container_mainactivity, ThankYouFragment())
                    .addToBackStack(null)
                    .commit()
            }
        })

        //confirm geklikt
        btn_reservation_confirm.setOnClickListener {
            placeReservation()
        }

        //cancel geklikt
        btn_reservation_cancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        //datum kiezen geklikt
        btn_reservation_pick_date.setOnClickListener {
            showDatePicker()
        }

        //uur kiezen geklikt
        btn_reservation_pick_time.setOnClickListener {
            showTimePicker()
        }
    }

    /**
     * Instantieer de listeners
     */
    @Suppress("UNUSED_EXPRESSION")
    private fun stopListeners() {
        //confirm geklikt
        btn_reservation_confirm.setOnClickListener { null }

        //cancel geklikt
        btn_reservation_cancel.setOnClickListener { null }

        //datum kiezen geklikt
        btn_reservation_pick_date.setOnClickListener { null }

        //uur kiezen geklikt
        btn_reservation_pick_time.setOnClickListener { null }
    }

    /**
     * Toont een timepicker en stelt de gekozen tijd bij de [ReservationViewModel] in.
     */
    private fun showTimePicker() {
        val today = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            reservationViewModel.hour = hour
            reservationViewModel.minute = minute

            btn_reservation_pick_time.text =
                    getString(R.string.text_time, String.format("%02d", hour), String.format("%02d", minute))
        }
        TimePickerDialog(
            activity,
            timeSetListener,
            today.get(Calendar.HOUR_OF_DAY),
            today.get(Calendar.MINUTE),
            true
        ).show()
    }

    /**
     * Toont een datepicker en stelt de gekozen datum bij de [ReservationViewModel] in.
     */
    private fun showDatePicker() {
        val today = Calendar.getInstance()

        val dpd =
            DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    reservationViewModel.year = year
                    reservationViewModel.month = monthOfYear + 1
                    reservationViewModel.day = dayOfMonth
                    btn_reservation_pick_date.text =
                            getString(R.string.text_date, dayOfMonth, (monthOfYear + 1), year)
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            )
        dpd.show()
    }

    /**
     * Probeert de reservatie te plaatsen, toast indien er velden ontbreken.
     */
    private fun placeReservation() {
        if (text_reservation_amount.text.toString().toIntOrNull() != null)
            reservationViewModel.amount = text_reservation_amount.text.toString().toInt()

        reservationViewModel.message = text_reservation_message.text.toString()

        if (reservationViewModel.valid()) {
            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(true)
            builder.setTitle(getString(R.string.text_confirm_reservation))
            builder.setMessage(
                getString(
                    R.string.question_want_to_order,
                    reservationViewModel.amount,
                    reservationViewModel.selectedLunch.value!!.name,
                    getString(
                        R.string.text_date,
                        reservationViewModel.day,
                        reservationViewModel.month,
                        reservationViewModel.year
                    ),
                    getString(
                        R.string.text_time,
                        String.format("%02d", reservationViewModel.hour),
                        String.format("%02d", reservationViewModel.minute)
                    )
                )
            )

            builder.setPositiveButton(getString(R.string.text_yes)) { _, _ -> reservationViewModel.placeReservation() }
            builder.setNegativeButton(getString(R.string.text_no)) { dialog, _ -> dialog.cancel() }

            val dialog = builder.create()
            dialog.show()
        } else {
            MessageUtil.showToast(getString(R.string.warning_empty_fields))
        }
    }

    override fun onStart() {
        super.onStart()
        GuiUtil.setActionBarTitle(requireActivity() as MainActivity, getString(R.string.text_reservation_place))
        GuiUtil.setCanPop(requireActivity() as MainActivity)
        initListeners()
    }

    override fun onStop() {
        stopListeners()
        GuiUtil.removeCanPop(requireActivity() as MainActivity)
        reservationViewModel.clear()
        super.onStop()
    }

}
