package com.greebiestudios.test_data_generator.ocr

import net.sourceforge.tess4j.Tesseract

trait OCR {
  val tesseract: Tesseract = new Tesseract
  tesseract.setDatapath("/usr/local/share/tessdata")
}
