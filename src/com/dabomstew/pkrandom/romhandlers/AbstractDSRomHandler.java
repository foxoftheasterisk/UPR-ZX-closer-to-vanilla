package com.dabomstew.pkrandom.romhandlers;

import java.awt.image.BufferedImage;
/*----------------------------------------------------------------------------*/
/*--  AbstractDSRomHandler.java - a base class for DS rom handlers          --*/
/*--                              which standardises common DS functions.   --*/
/*--                                                                        --*/
/*--  Part of "Universal Pokemon Randomizer ZX" by the UPR-ZX team          --*/
/*--  Originally part of "Universal Pokemon Randomizer" by Dabomstew        --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2020.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.dabomstew.pkrandom.FileFunctions;
import com.dabomstew.pkrandom.GFXFunctions;
import com.dabomstew.pkrandom.RomFunctions;
import com.dabomstew.pkrandom.exceptions.CannotWriteToLocationException;
import com.dabomstew.pkrandom.exceptions.RandomizerIOException;
import com.dabomstew.pkrandom.graphics.Palette;
import com.dabomstew.pkrandom.newnds.NARCArchive;
import com.dabomstew.pkrandom.newnds.NDSRom;
import com.dabomstew.pkrandom.pokemon.Pokemon;
import com.dabomstew.pkrandom.pokemon.Type;

import javax.imageio.ImageIO;

public abstract class AbstractDSRomHandler extends AbstractRomHandler {
	
    private static final byte[] PALETTE_PREFIX_BYTES = { (byte) 0x52, (byte) 0x4C, (byte) 0x43, (byte) 0x4E,
            (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x01, (byte) 0x48, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x10, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x54, (byte) 0x54, (byte) 0x4C, (byte) 0x50,
            (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x0A, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    protected String dataFolder;
    private NDSRom baseRom;
    private String loadedFN;
    private boolean arm9Extended = false;

    public AbstractDSRomHandler(Random random, PrintStream logStream) {
        super(random, logStream);
    }

    protected abstract boolean detectNDSRom(String ndsCode, byte version);

    @Override
    public boolean loadRom(String filename) {
        if (!this.detectNDSRom(getROMCodeFromFile(filename), getVersionFromFile(filename))) {
            return false;
        }
        // Load inner rom
        try {
            baseRom = new NDSRom(filename);
        } catch (IOException e) {
            throw new RandomizerIOException(e);
        }
        loadedFN = filename;
        loadedROM(baseRom.getCode(), baseRom.getVersion());
        return true;
    }

    @Override
    public String loadedFilename() {
        return loadedFN;
    }

    protected byte[] get3byte(int amount) {
        byte[] ret = new byte[3];
        ret[0] = (byte) (amount & 0xFF);
        ret[1] = (byte) ((amount >> 8) & 0xFF);
        ret[2] = (byte) ((amount >> 16) & 0xFF);
        return ret;
    }

    protected abstract void loadedROM(String romCode, byte version);

    @Override
    public boolean saveRomFile(String filename, long seed) {
        try {
            baseRom.saveTo(filename);
        } catch (IOException e) {
            if (e.getMessage().contains("Access is denied")) {
                throw new CannotWriteToLocationException("The randomizer cannot write to this location: " + filename);
            } else {
                throw new RandomizerIOException(e);
            }
        }
        return true;
    }

    @Override
    public boolean saveRomDirectory(String filename) {
        // do nothing. DS games do have the concept of a filesystem, but it's way more
        // convenient for users to use ROM files instead.
        return true;
    }

    @Override
    public boolean hasGameUpdateLoaded() {
        return false;
    }

    @Override
    public boolean loadGameUpdate(String filename) {
        // do nothing, as DS games don't have external game updates
        return true;
    }

    @Override
    public void removeGameUpdate() {
        // do nothing, as DS games don't have external game updates
    }

    @Override
    public String getGameUpdateVersion() {
        // do nothing, as DS games don't have external game updates
        return null;
    }

    @Override
    public void printRomDiagnostics(PrintStream logStream) {
        baseRom.printRomDiagnostics(logStream);
    }

    public void closeInnerRom() throws IOException {
        baseRom.closeROM();
    }

    @Override
    public boolean canChangeStaticPokemon() {
        return false;
    }

    @Override
    public boolean hasPhysicalSpecialSplit() {
        // Default value for Gen4+.
        // Handlers can override again in case of ROM hacks etc.
        return true;
    }

    public NARCArchive readNARC(String subpath) throws IOException {
        return new NARCArchive(readFile(subpath));
    }

    public void writeNARC(String subpath, NARCArchive narc) throws IOException {
        this.writeFile(subpath, narc.getBytes());
    }

    protected static String getROMCodeFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            fis.skip(0x0C);
            byte[] sig = FileFunctions.readFullyIntoBuffer(fis, 4);
            fis.close();
            return new String(sig, "US-ASCII");
        } catch (IOException e) {
            throw new RandomizerIOException(e);
        }
    }

    protected static byte getVersionFromFile(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            fis.skip(0x1E);
            byte[] version = FileFunctions.readFullyIntoBuffer(fis, 1);
            fis.close();
            return version[0];
        } catch (IOException e) {
            throw new RandomizerIOException(e);
        }
    }

    protected int readByte(byte[] data, int offset) { return data[offset] & 0xFF; }

    protected int readWord(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8);
    }

    protected int readLong(byte[] data, int offset) {
        return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8) | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    protected int readRelativePointer(byte[] data, int offset) {
        return readLong(data, offset) + offset + 4;
    }

    protected void writeWord(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    protected void writeLong(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    protected void writeRelativePointer(byte[] data, int offset, int pointer) {
        int relPointer = pointer - (offset + 4);
        writeLong(data, offset, relPointer);
    }

    protected byte[] readFile(String location) throws IOException {
        return baseRom.getFile(location);
    }

    protected void writeFile(String location, byte[] data) throws IOException {
        writeFile(location, data, 0, data.length);
    }

    protected void writeFile(String location, byte[] data, int offset, int length) throws IOException {
        if (offset != 0 || length != data.length) {
            byte[] newData = new byte[length];
            System.arraycopy(data, offset, newData, 0, length);
            data = newData;
        }
        baseRom.writeFile(location, data);
    }

    protected byte[] readARM9() throws IOException {
        return baseRom.getARM9();
    }

    protected void writeARM9(byte[] data) throws IOException {
        baseRom.writeARM9(data);
    }

    protected byte[] readOverlay(int number) throws IOException {
        return baseRom.getOverlay(number);
    }

    protected void writeOverlay(int number, byte[] data) throws IOException {
        baseRom.writeOverlay(number, data);
    }

    protected void readByteIntoFlags(byte[] data, boolean[] flags, int offsetIntoFlags, int offsetIntoData) {
        int thisByte = data[offsetIntoData] & 0xFF;
        for (int i = 0; i < 8 && (i + offsetIntoFlags) < flags.length; i++) {
            flags[offsetIntoFlags + i] = ((thisByte >> i) & 0x01) == 0x01;
        }
    }

    protected byte getByteFromFlags(boolean[] flags, int offsetIntoFlags) {
        int thisByte = 0;
        for (int i = 0; i < 8 && (i + offsetIntoFlags) < flags.length; i++) {
            thisByte |= (flags[offsetIntoFlags + i] ? 1 : 0) << i;
        }
        return (byte) thisByte;
    }

    protected int typeTMPaletteNumber(Type t) {
        if (t == null) {
            return 411; // CURSE
        }
        switch (t) {
        case FIGHTING:
            return 398;
        case DRAGON:
            return 399;
        case WATER:
            return 400;
        case PSYCHIC:
            return 401;
        case NORMAL:
            return 402;
        case POISON:
            return 403;
        case ICE:
            return 404;
        case GRASS:
            return 405;
        case FIRE:
            return 406;
        case DARK:
            return 407;
        case STEEL:
            return 408;
        case ELECTRIC:
            return 409;
        case GROUND:
            return 410;
        case GHOST:
        default:
            return 411; // for CURSE
        case ROCK:
            return 412;
        case FLYING:
            return 413;
        case BUG:
            return 610;
        }
    }

    private int find(byte[] data, String hexString) {
        if (hexString.length() % 2 != 0) {
            return -3; // error
        }
        byte[] searchFor = new byte[hexString.length() / 2];
        for (int i = 0; i < searchFor.length; i++) {
            searchFor[i] = (byte) Integer.parseInt(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        List<Integer> found = RomFunctions.search(data, searchFor);
        if (found.size() == 0) {
            return -1; // not found
        } else if (found.size() > 1) {
            return -2; // not unique
        } else {
            return found.get(0);
        }
    }

    protected byte[] extendARM9(byte[] arm9, int extendBy, String prefix, int arm9Offset) {
        /*
        Simply extending the ARM9 at the end doesn't work. Towards the end of the ARM9, the following sections exist:
        1. A section that is copied to ITCM (Instruction Tightly Coupled Memory)
        2. A section that is copied to DTCM (Data Tightly Coupled Memory)
        3. Pointers specifying to where these sections should be copied as well as their sizes

        All of these sections are later overwritten(!) and the area is used more or less like a regular RAM area.
        This means that if any new code is put after these sections, it will also be overwritten.
        Changing which area is overwritten is not viable. There are very many pointers to this area that would need to
        be re-indexed.

        Our solution is to extend the section that is to be copied to ITCM, so that any new code gets copied to
        ITCM and can be executed from there. This means we have to shift all the data that is after this in order to
        make space. Additionally, elsewhere in the ARM9, pointers are stored specifying from where the ITCM
        section should be copied, as well as some other data. They are supposedly part of some sort of NDS library
        functions and should work the same across games; look for "[SDK+NINTENDO:" in the ARM9 and these pointers should
        be slightly before that. They are as follows (each pointer = 4 bytes):
        1. Pointer specifying from where the destination pointers/sizes should be read (see point 3 above)
        2. Pointer specifying the end address of the ARM9.
        3. Pointer specifying from where data copying should start (since ITCM is first, this corresponds to the start
           of the section that should be copied to ITCM).
        4. Pointer specifying where data should start being overwritten. (should be identical to #3)
        5. Pointer specifying where data should stop being overwritten (should correspond to start of ovl table).
        6. ???

        Out of these, we want to change #1 (it will be moved because we have to shift the end of the ARM9 to make space
        for enlarging the "copy to ITCM" area) and #2 (since the ARM9 will be made larger). We also want to change the
        specified size for the ITCM area since we're enlarging it.
         */

        if (arm9Extended) return arm9;  // Don't try to extend the ARM9 more than once

        int tcmCopyingPointersOffset = find(arm9, prefix);
        tcmCopyingPointersOffset += prefix.length() / 2; // because it was a prefix

        int oldDestPointersOffset = FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset) - arm9Offset;
        int itcmSrcOffset =
                FileFunctions.readFullInt(arm9, tcmCopyingPointersOffset + 8) - arm9Offset;
        int itcmSizeOffset = oldDestPointersOffset + 4;
        int oldITCMSize = FileFunctions.readFullInt(arm9, itcmSizeOffset);

        int oldDTCMOffset = itcmSrcOffset + oldITCMSize;

        byte[] newARM9 = Arrays.copyOf(arm9, arm9.length + extendBy);

        // Change:
        // 1. Pointer to destination pointers/sizes
        // 2. ARM9 size
        // 3. Size of the area copied to ITCM
        FileFunctions.writeFullInt(newARM9, tcmCopyingPointersOffset,
                oldDestPointersOffset + extendBy + arm9Offset);
        FileFunctions.writeFullInt(newARM9, tcmCopyingPointersOffset + 4,
                newARM9.length + arm9Offset);
        FileFunctions.writeFullInt(newARM9, itcmSizeOffset, oldITCMSize + extendBy);

        // Finally, shift everything
        System.arraycopy(newARM9, oldDTCMOffset, newARM9, oldDTCMOffset + extendBy,
                arm9.length - oldDTCMOffset);

        arm9Extended = true;

        return newARM9;
    }
    
	private byte[] concatenate(byte[] a, byte[] b) {
	    byte[] sum = new byte[a.length + b.length];
	    System.arraycopy(a, 0, sum, 0, a.length);
	    System.arraycopy(b, 0, sum, a.length, b.length);
	    return sum;
	}

	// I dare not rewrite the load ROM structure, so for now loadPokemonPalettes()
	// is separate methods called in loadROM()/loadedRom() methods. Even though
	// one call in AbstractRomHandler should suffice.
	protected void loadPokemonPalettes() {
        try {
            String NARCpath = getNARCPath("PokemonGraphics");
            NARCArchive pokeGraphicsNARC = readNARC(NARCpath);
            for (Pokemon pk : getPokemonSet()) {
                int normalPaletteIndex = calculatePokemonNormalPaletteIndex(pk.getNumber());
                pk.setNormalPalette(readPalette(pokeGraphicsNARC, normalPaletteIndex));
                
                int shinyPaletteIndex = calculatePokemonShinyPaletteIndex(pk.getNumber());
                pk.setShinyPalette(readPalette(pokeGraphicsNARC, shinyPaletteIndex));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected abstract int calculatePokemonNormalPaletteIndex(int i);
    
    protected abstract int calculatePokemonShinyPaletteIndex(int i);

    protected final Palette readPalette(NARCArchive NARC, int index) {
        byte[] withPrefixBytes = NARC.files.get(index);
        byte[] paletteBytes = Arrays.copyOfRange(withPrefixBytes, PALETTE_PREFIX_BYTES.length, withPrefixBytes.length);
        return new Palette(paletteBytes);
    }

    @Override
    public void savePokemonPalettes() {
        try {
            String NARCpath = getNARCPath("PokemonGraphics");
            NARCArchive pokeGraphicsNARC = readNARC(NARCpath);

            for (Pokemon pk : getPokemonSet()) {

                int normalPaletteIndex = calculatePokemonNormalPaletteIndex(pk.getNumber());
                byte[] normalPaletteBytes = pk.getNormalPalette().toBytes();
                normalPaletteBytes = concatenate(PALETTE_PREFIX_BYTES, normalPaletteBytes);
                pokeGraphicsNARC.files.set(normalPaletteIndex, normalPaletteBytes);
                
                int shinyPaletteIndex = calculatePokemonShinyPaletteIndex(pk.getNumber());
                byte[] shinyPaletteBytes = pk.getShinyPalette().toBytes();
                shinyPaletteBytes = concatenate(PALETTE_PREFIX_BYTES, shinyPaletteBytes);
                pokeGraphicsNARC.files.set(shinyPaletteIndex, shinyPaletteBytes);

            }
            writeNARC(NARCpath, pokeGraphicsNARC);

        } catch (IOException e) {
            throw new RandomizerIOException(e);
        }
    }
    
	@Override
	protected List<BufferedImage> getAllPokemonImages() {
        ripAllOtherPokes();
		List<BufferedImage> bims = new ArrayList<>();

		String NARCPath = getNARCPath("PokemonGraphics");
		NARCArchive pokeGraphicsNARC;
		try {
			pokeGraphicsNARC = readNARC(NARCPath);
		} catch (IOException e) {
			throw new RandomizerIOException(e);
		}

		for (Pokemon pk : getPokemonSet()) {
			BufferedImage frontNormal = getPokemonImage(pk, pokeGraphicsNARC, false, false, false, true);
			BufferedImage backNormal = getPokemonImage(pk, pokeGraphicsNARC, true, false, false, false);
			BufferedImage frontShiny = getPokemonImage(pk, pokeGraphicsNARC, false, true, false, true);
			BufferedImage backShiny = getPokemonImage(pk, pokeGraphicsNARC, true, true, false, false);

			BufferedImage combined = GFXFunctions
					.stitchToGrid(new BufferedImage[][] { { frontNormal, backNormal }, { frontShiny, backShiny } });
			bims.add(combined);
		}
		return bims;
	}

    private void ripAllOtherPokes() {
        String NARCPath = getNARCPath("OtherPokemonGraphics");
        NARCArchive pokeGraphicsNARC;
        try {
            pokeGraphicsNARC = readNARC(NARCPath);
        } catch (IOException e) {
            throw new RandomizerIOException(e);
        }
        for (int i=0; i <= 157; i++) {
            ripAndDumpOtherPokemon(pokeGraphicsNARC, i);
        }

//        ripAndDumpOtherPokemon(pokeGraphicsNARC, 208);
//        ripAndDumpOtherPokemon(pokeGraphicsNARC, 209);
//        ripAndDumpOtherPokemon(pokeGraphicsNARC, 211);
    }

    private void ripAndDumpOtherPokemon(NARCArchive pokeGraphicsNARC, int i) {
        BufferedImage bim = ripOtherPoke(i, pokeGraphicsNARC);
        String fileAdress = "Pokemon_sprite_dump/gen" + generationOfPokemon() + "/"
                + String.format("a_%03d.png", i);
        File outputfile = new File(fileAdress);
        try {
            ImageIO.write(bim, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract BufferedImage ripOtherPoke(int i, NARCArchive pokeGraphicsNARC);

    @Override
	public final BufferedImage getMascotImage() {
		try {
			dumpAllPokemonSprites();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Pokemon pk = randomPokemon();
			String NARCpath = getNARCPath("PokemonGraphics");
			NARCArchive pokeGraphicsNARC = readNARC(NARCpath);
			boolean shiny = random.nextInt(10) == 0;

			return getPokemonImage(pk, pokeGraphicsNARC, false, shiny, true, false);
		} catch (IOException e) {
			throw new RandomizerIOException(e);
		}
	}
    
    // TODO: Using many boolean arguments is suboptimal in Java, but I am unsure of the pattern to replace it
	public abstract BufferedImage getPokemonImage(Pokemon pk, NARCArchive pokeGraphicsNARC, boolean back, boolean shiny,
			boolean transparentBackground, boolean includePalette);

    // TODO: remove when done testing
    public abstract BufferedImage getPokemonImage(int number, NARCArchive pokeGraphicsNARC, boolean back, boolean shiny,
                                         boolean transparentBackground, boolean includePalette);

    // because RomEntry is an inner class it can't be accessed here, so an abstract
    // method is needed.
    // a refactoring might be better, but is outside of the scope for the changes
    // I'm making now
    // - voliol 2022-01-13
    public abstract String getNARCPath(String fileName);

}
