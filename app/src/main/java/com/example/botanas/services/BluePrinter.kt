package com.example.botanas.services

import android.content.Context
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter

class BluePrinter(context: Context) {
    init {
        Printooth.init(context)
    }

    fun printSale() {
        var printables = ArrayList<Printable>()
        var printable = TextPrintable.Builder()
            .setText("Hello World") //The text you want to print
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD) //Bold or normal
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON) // Underline on/off
            .setCharacterCode(DefaultPrinter.CHARCODE_PC850) // Character code to support languages
            .setLineSpacing(DefaultPrinter.LINE_SPACING_60)
            .setNewLinesAfter(1) // To provide n lines after sentence
            .build()
        printables.add(printable)
        Printooth.printer().print(printables)
    }
}