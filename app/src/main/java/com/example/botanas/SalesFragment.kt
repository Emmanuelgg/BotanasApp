package com.example.botanas

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.botanas.adapter.SalesAdapter
import com.example.botanas.dataClasses.Requisition
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SalesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SalesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SalesFragment : Fragment(), SalesAdapter.ItemOnPressListener {


    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mySqlHelper: MySqlHelper
    private val requisitionList = ArrayList<Requisition>()
    private lateinit var appContext: Context
    private lateinit var salesRecyclerView: RecyclerView
    private lateinit var salesAdapter: SalesAdapter

    override fun onItemClick(item: SalesAdapter.ViewHolder, position: Int) {
        val intent = Intent(appContext, SaleDetail::class.java)
        intent.putExtra("id_requisition", requisitionList[position].id_requisition)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        appContext = this.context!!
        mySqlHelper = MySqlHelper(appContext)
        initRecycleView()
        salesAdapter = SalesAdapter(requisitionList,this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sales, container, false)
        salesRecyclerView = view.findViewById(R.id.sales_recycler_view)
        salesRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(appContext)
            this.adapter = salesAdapter
        }

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SalesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            SalesFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private fun initRecycleView(){
        mySqlHelper.use {
            select("requisition")
                .exec {
                    if  (this.count > 0)
                        while(this.moveToNext()){
                            var clientName = ""
                            select("client", "name")
                                .whereArgs("id_client == {id_client}",
                                    "id_client" to this.getInt(this.getColumnIndex("id_client"))
                                ).exec {
                                    this.moveToNext()
                                    clientName = this.getString(this.getColumnIndex("name"))
                                }
                            requisitionList.add(
                                Requisition(
                                    this.getInt(this.getColumnIndex("id_requisition")),
                                    clientName,
                                    this.getString(this.getColumnIndex("created_at")),
                                    this.getString(this.getColumnIndex("total"))
                                )
                            )
                        }
                    else
                        requisitionList.add(
                            Requisition(
                                0,
                                "No se han realizado ventas",
                                "",
                                "0"
                            )
                        )
                }
        }
    }
}
