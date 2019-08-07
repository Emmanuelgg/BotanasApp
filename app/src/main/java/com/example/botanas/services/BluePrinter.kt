package com.example.botanas.services

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.BitmapFactory
import com.example.botanas.R
import com.example.botanas.db.MySqlHelper
import com.example.botanas.ui.login.Admin
import org.jetbrains.anko.db.select
import java.io.OutputStream
import java.text.NumberFormat








class BluePrinter(context: Context) {
    companion object {
        var device: BluetoothDevice? = null
        private lateinit var mBTSocket: BluetoothSocket
        private lateinit var os: OutputStream
        private var connected = false
        private lateinit var uuid: BluetoothSocket
    }

    private val context = context

    init {

    }

    fun connected(): Boolean {
        return connected
    }

    fun setPrinter(device: BluetoothDevice): Boolean {
        try {
            if (!connected()) {
                uuid = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
                mBTSocket = uuid
                mBTSocket.connect()
                os = mBTSocket.outputStream
                os.flush()
                connected = true
                return true

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun unsetPrinter() {
        if  (connected()) {
            mBTSocket.close()
            connected = false
        }

    }

    fun printSale(idRequisition: Int) {
        os.run {
            write(PrinterCommands.INIT)
            write(PrinterCommands.CHARCODE_MULTI)
        }
        val bitMap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_botanas_200x50)

        val image = PrinterUtils.decodeBitmap(bitMap)

        try {
            val mySqlHelper = MySqlHelper(context)
            val currency = NumberFormat.getCurrencyInstance()

            val title = "Botanas Anita".toByteArray()
            os.apply {
                write(PrinterCommands.FONT_SIZE_LARGE)
                write(PrinterCommands.ESC_ALIGN_CENTER)
               /* write(title)
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FEED_LINE)
                */
                write(PrinterCommands.ESC_HORIZONTAL_CENTERS)
                write(PrinterCommands.ESC_ALIGN_CENTER)
                write(image)
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FEED_LINE)


            }
            var clientName: ByteArray = byteArrayOf()
            var subTotal = 0.00
            var total = 0.00
            var date: ByteArray = byteArrayOf()
            var discountPercentage = 0.00
            mySqlHelper.use {
                select("requisition")
                    .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                    .exec {
                        this.moveToNext()
                        val idClient = this.getInt(this.getColumnIndex("id_client"))
                        discountPercentage = this.getDouble(this.getColumnIndex("discount"))
                        total = this.getDouble(this.getColumnIndex("total"))
                        date = ("Fecha: " + this.getString(this.getColumnIndex("requisition_date"))).toByteArray()
                        select("client")
                            .whereArgs("id_client == {id_client}", "id_client" to idClient)
                            .exec {
                                this.moveToNext()
                                clientName = (this.getString(this.getColumnIndex("name"))).toByteArray()
                            }
                    }
            }
            os.apply {
                write(PrinterCommands.FONT_SIZE_MEDIUM)
                write(PrinterCommands.ESC_ALIGN_LEFT)
                write(PrinterCommands.ESC_CANCEL_BOLD)
                write(("Chofer: "+Admin.name).toByteArray())
                write(PrinterCommands.FEED_LINE)
                write(date)
                write(PrinterCommands.FEED_LINE)
                write(clientName)
                write(PrinterCommands.FEED_LINE)
                write("-------------------------------".toByteArray())
                write(PrinterCommands.FEED_LINE)
            }
            mySqlHelper.use {
                select("requisition_description")
                    .whereArgs("id_requisition == {id_requisition}", "id_requisition" to idRequisition)
                    .exec {
                        while (this.moveToNext()) {

                            val quantity = this.getString(this.getColumnIndex("quantity"))+" x"
                            val name = this.getString(this.getColumnIndex("description"))
                            val price = this.getString(this.getColumnIndex("price"))
                            val totalProduct =  "%.2f".format(this.getDouble(this.getColumnIndex("total")))
                            subTotal += this.getDouble(this.getColumnIndex("total"))
                            os.apply {

                                write(PrinterCommands.FONT_SIZE_SMALL)
                                write(PrinterCommands.ESC_CANCEL_BOLD)
                                write(PrinterCommands.ESC_ALIGN_LEFT)
                                write(PrinterCommands.SET_LINE_SPACING_16)
                                write(name.toByteArray())
                                write(PrinterCommands.FEED_LINE)

                                write(PrinterCommands.ESC_ALIGN_RIGHT)

                                write(quantity.toByteArray())
                                write(PrinterCommands.BLANK_SPACE)
                                write(price.toByteArray())
                                write(" =".toByteArray())
                                write(PrinterCommands.BLANK_SPACE)
                                write(PrinterCommands.SET_LINE_SPACING_30)
                                write(totalProduct.toByteArray())
                                write(PrinterCommands.FEED_LINE)
                            }

                        }
                    }
            }
            discountPercentage /= 100
            val discount = if (discountPercentage != 0.00)
                "%.2f".format(subTotal*discountPercentage+1).toByteArray()
            else
                "%.2f".format(0.00).toByteArray()

            val subTotalString = "%.2f".format(subTotal).toByteArray()
            total -= (total * discountPercentage)
            val totalString = currency.format(total).toByteArray()
            os.apply {
                write(PrinterCommands.SET_LINE_SPACING_30)
                write(PrinterCommands.ESC_ALIGN_RIGHT)
                write("-----------------------".toByteArray())
                write(PrinterCommands.FEED_LINE)
                write("Subtotal:".toByteArray())
                write(PrinterCommands.BLANK_SPACE)
                write(PrinterCommands.BLANK_SPACE)
                //write(PrinterCommands.ESC_ALIGN_RIGHT)
                write(subTotalString)
                write(PrinterCommands.FEED_LINE)

                /*write("Descuento %:".toByteArray())
                write(PrinterCommands.BLANK_SPACE)
                write(PrinterCommands.BLANK_SPACE)
                write("${(discountPercentage*100)} %".toByteArray())
                write(PrinterCommands.FEED_LINE)*/

                //write(PrinterCommands.ESC_ALIGN_LEFT)
                write("Descuento:".toByteArray())
                write(PrinterCommands.BLANK_SPACE)
                write(PrinterCommands.BLANK_SPACE)
                //write(PrinterCommands.ESC_ALIGN_RIGHT)
                write(discount)
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FEED_LINE)

                //write(PrinterCommands.ESC_ALIGN_LEFT)
                write(PrinterCommands.ESC_ALIGN_CENTER)
                write(PrinterCommands.FONT_SIZE_SMALL)
                write(PrinterCommands.ESC_BOLD)
                write("Total".toByteArray())
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FONT_SIZE_MEDIUM)
                write(totalString)
                write(PrinterCommands.FEED_LINE)
                write("-------------------------------".toByteArray())
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FEED_LINE)
                write(PrinterCommands.FEED_LINE)

            }


        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}

object PrinterCommands {
    val HT: Byte = 0x9
    val LF: Byte = 0x0A
    val CR: Byte = 0x0D
    val ESC: Byte = 0x1B
    val DLE: Byte = 0x10
    val GS: Byte = 0x1D
    val FS: Byte = 0x1C
    val STX: Byte = 0x02
    val US: Byte = 0x1F
    val CAN: Byte = 0x18
    val CLR: Byte = 0x0C
    val EOT: Byte = 0x04
    //val CHARCODE_PC850 = byteArrayOf(27, 116, 0x02)
    val CHARCODE_MULTI = byteArrayOf(27, 116, 2)
    val FONT_SIZE_SMALL = byteArrayOf(27, 33, 13)
    //val FONT_SIZE_SMALL = byteArrayOf(29, 33, 0x02)
    val FONT_SIZE_MEDIUM = byteArrayOf(27, 33, 6)
    val FONT_SIZE_LARGE = byteArrayOf(27, 33, 127)
    //val FONT_SIZE_LARGE = byteArrayOf(29, 33, 0x10)
    val SET_LINE = "\n".toByteArray()
    val BLANK_SPACE = " ".toByteArray()


    val INIT = byteArrayOf(0x1B, 0x40)
    var FEED_LINE = byteArrayOf(10)

    var SELECT_FONT_A = byteArrayOf(20, 33, 0)

    var SET_BAR_CODE_HEIGHT = byteArrayOf(29, 104, 100)
    var PRINT_BAR_CODE_1 = byteArrayOf(29, 107, 2)
    var SEND_NULL_BYTE = byteArrayOf(0x00)

    var SELECT_PRINT_SHEET = byteArrayOf(0x1B, 0x63, 0x30, 0x02)
    var FEED_PAPER_AND_CUT = byteArrayOf(0x1D, 0x56, 66, 0x00)

    var SELECT_CYRILLIC_CHARACTER_CODE_TABLE = byteArrayOf(0x1B, 0x74, 0x11)

    var SELECT_BIT_IMAGE_MODE = byteArrayOf(0x1B, 0x2A, 33, -128, 0)
    var SET_LINE_SPACING_16 = byteArrayOf(0x1B, 0x33, 10)
    var SET_LINE_SPACING_20 = byteArrayOf(0x1B, 0x33, 20)
    var SET_LINE_SPACING_24 = byteArrayOf(0x1B, 0x33, 24)
    var SET_LINE_SPACING_30 = byteArrayOf(0x1B, 0x33, 30)

    var TRANSMIT_DLE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x01)
    var TRANSMIT_DLE_OFFLINE_PRINTER_STATUS = byteArrayOf(0x10, 0x04, 0x02)
    var TRANSMIT_DLE_ERROR_STATUS = byteArrayOf(0x10, 0x04, 0x03)
    var TRANSMIT_DLE_ROLL_PAPER_SENSOR_STATUS = byteArrayOf(0x10, 0x04, 0x04)

    val ESC_FONT_COLOR_DEFAULT = byteArrayOf(0x1B, 'r'.toByte(), 0x00)
    val FS_FONT_ALIGN = byteArrayOf(0x1C, 0x21, 1, 0x1B, 0x21, 1)
    val ESC_ALIGN_LEFT = byteArrayOf(0x1b, 'a'.toByte(), 0x00)
    val ESC_ALIGN_RIGHT = byteArrayOf(0x1b, 'a'.toByte(), 0x02)
    val ESC_ALIGN_CENTER = byteArrayOf(0x1b, 'a'.toByte(), 0x01)
    val ESC_CANCEL_BOLD = byteArrayOf(0x1B, 0x45, 0)
    val ESC_BOLD = byteArrayOf(0x1B, 0x45, 0x01)


    /** */
    val ESC_HORIZONTAL_CENTERS = byteArrayOf(0x1B, 0x44, 20, 28, 0)
    val ESC_CANCLE_HORIZONTAL_CENTERS = byteArrayOf(0x1B, 0x44, 0)
    /** */

    val ESC_ENTER = byteArrayOf(0x1B, 0x4A, 0x40)
    val PRINTE_TEST = byteArrayOf(0x1D, 0x28, 0x41)

}