package me.affanhaq.mapcha.player;

import com.hyd.captcha.CaptchaGenerator;
import com.hyd.captcha.FontRepository;
import com.hyd.captcha.background.CirclesBackground;
import me.affanhaq.mapcha.Mapcha;
import org.bukkit.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static me.affanhaq.mapcha.Mapcha.Config.*;

public class CaptchaPlayer {

    private final Player player;
    private final String captcha;

    private int tries;
    private final long lastTime;

    /**
     * @param player  the player
     * @param captcha the captcha string for the player
     * @param mapcha  JavaPlugin
     */
    public CaptchaPlayer(Player player, String captcha, Mapcha mapcha) {
        this.player = player;
        this.captcha = captcha;


        lastTime = System.currentTimeMillis();
        tries = 0;

        // starting a timer to kick the player if the captcha has not been finished
        player.getServer().getScheduler().scheduleSyncDelayedTask(mapcha, () -> {
            if (mapcha.getPlayerManager().getPlayer(player) != null) {
                player.getPlayer().kickPlayer(prefix + " " + failMessage);
            }
        }, timeLimit * 20);
    }

    /**
     * Renders the captcha.
     *
     * @return the rendered captcha
     */
    public BufferedImage render(Mapcha mapcha) throws IOException {
        String title = "Captcha";

        BufferedImage in = ImageIO.read(mapcha.getResource("egirl.png"));

        CaptchaGenerator captchaGenerator = new CaptchaGenerator();
        FontRepository.pickRandomFont();
        captchaGenerator.setBackground(new CirclesBackground());

        BufferedImage captchaImage = captchaGenerator.generate(130, 50, captcha);

        BufferedImage image = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawImage(in, 0, 0, null);
        g.drawString(title, (int) ((image.getWidth() - g.getFontMetrics().getStringBounds(title, g).getWidth()) / 2), 30);

        g.setFont(new Font("Helvetica", Font.BOLD, 10));

        String sTries = "Tries Left: ";
        g.setColor(Color.WHITE);
        g.drawString(sTries, (int) ((image.getWidth() - g.getFontMetrics().getStringBounds(sTries, g).getWidth()) / 2), 45);
        g.setColor((Mapcha.Config.tries - tries) == 1 ? Color.RED : Color.BLUE);
        g.drawString(String.valueOf((Mapcha.Config.tries - tries)), (int) (((image.getWidth() - g.getFontMetrics().getStringBounds(sTries, g).getWidth()) / 2) + g.getFontMetrics().getStringBounds(sTries, g).getWidth() + 2), 45);

        String sTime = "Time Left: ";
        g.setColor(Color.WHITE);
        g.drawString(sTime, (int) ((image.getWidth() - g.getFontMetrics().getStringBounds(sTime, g).getWidth()) / 2), 55);
        g.setColor((timeLimit * 1000) - (System.currentTimeMillis() - lastTime) == 1000 ? Color.RED : Color.BLUE);
        g.drawString(new SimpleDateFormat("ss").format((timeLimit * 1000) - (System.currentTimeMillis() - lastTime)), (int) (((image.getWidth() - g.getFontMetrics().getStringBounds(sTime, g).getWidth()) / 2) + g.getFontMetrics().getStringBounds(sTime, g).getWidth() + 2), 55);

        g.setFont(new Font("Roboto", Font.BOLD, 40));
        g.setColor(Color.WHITE);
        g.drawImage(captchaImage, 0, 80, null);
        //g.drawString(captcha, (int) ((image.getWidth() - g.getFontMetrics().getStringBounds(captcha, g).getWidth()) / 2), 105);

        return image;
    }

    public String getCaptcha() {
        return captcha;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTries() {
        return tries;
    }

    public void setTries(int tries) {
        this.tries = tries;
    }

}