package dk.sdu.imada.gui.plots;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import dk.sdu.imada.gui.controllers.FXPopOutMsg;

public class PlotUtil {

	public static void exportChart(BufferedImage bufferedImage, String out) {
		try {

			File outPutFile = new File(out);

			if (outPutFile!=null) {
				ImageIO.write(bufferedImage, "png", outPutFile);
			}

		}catch(IOException e) {
			FXPopOutMsg.showWarning("Can't save file");
		}
	}
}
