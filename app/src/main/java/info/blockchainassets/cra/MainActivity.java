package info.blockchainassets.cra;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private Web3j web3;
    private String password;
    private String walletPath;
    private String walletName;
    private File walletDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up main activity UI
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        NavigationView nvDrawer = findViewById(R.id.nv);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(nvDrawer);

        // Auxiliary method to fix EC-related problems specific to Android
        setupBouncyCastle();

        //Get password from file (test-only)
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("password.txt")));

            // do reading, usually loop until end of file reading
            password = reader.readLine();
        } catch (IOException e) {
            toastAsync(e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    toastAsync(e.getMessage());
                }
            }
        }

        // The wallet path is the default 'files' location, where the wallet will be created
        walletPath = getFilesDir().getAbsolutePath();
        walletDir = new File(walletPath);
    }

    public void connectToEthNetwork(View v) {
        toastAsync("Connecting to Ethereum network...");
        web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/9e4fadd65d4e4bbab2032b259b0979c3"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
                toastAsync("Connected!");
            }
            else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void createWallet(View v){
        try{
            walletName = WalletUtils.generateNewWalletFile(password, walletDir);
            toastAsync("Wallet generated");
        }
        catch (Exception e){
            toastAsync(e.getMessage());
        }
    }

    public void getAddress(View v){
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir + "/" + walletName);
            toastAsync("Your address is " + credentials.getAddress());
        }
        catch (Exception e){
            toastAsync(e.getMessage());
        }
    }

    public void toastAsync(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectItemDrawer(MenuItem menuItem) {
        Fragment myFragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.home:
                fragmentClass = Home.class;
                break;
            case R.id.portfolio:
                fragmentClass = Portfolio.class;
                break;
            case R.id.risk_management:
                fragmentClass = RiskManagement.class;
                break;
            case R.id.notifications:
                fragmentClass = Notifications.class;
                break;
            case R.id.settings:
                fragmentClass = Settings.class;
                break;
            default:
                fragmentClass = Home.class;
        }
        try {
            myFragment = (Fragment) fragmentClass.newInstance();
        } catch(Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager;
        fragmentManager = getSupportFragmentManager();
        assert myFragment != null;
        fragmentManager.beginTransaction().replace(R.id.flcontent,myFragment).commit();
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawers();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                selectItemDrawer(menuItem);
                return true;
            }
        });
    }

    // Fixes the "No such algorithm: ECDSA for provider BC" error,
    // see https://github.com/web3j/web3j/issues/915
    private void setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return;
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
