package org.daisy.pipeline.yourkit.probes;

import com.yourkit.probes.MethodPattern;
import com.yourkit.probes.ObjectRowIndexMap;
import com.yourkit.probes.OnEnterResult;
import com.yourkit.probes.Param;
import com.yourkit.probes.StringColumn;
import com.yourkit.probes.Table;
import com.yourkit.probes.This;

public final class Liblouis {
	
	@MethodPattern("org.liblouis.Translator:<init>(String)")
	public static final class CompileProbe {
		public static void onEnter(@This Object translator,
		                           @Param(1) String table) {
			TRANSLATORS.createRow(translator, table);
		}
	}
	
	@MethodPattern("org.liblouis.Translator:translate(String, *)")
	public static final class TranslateProbe {
		public static int onEnter(@This Object translator,
		                           @Param(1) String input) {
			return TRANSLATIONS.createRow(translator, input);
		}
		public static void onReturn(@OnEnterResult int row) {
			TRANSLATIONS.closeRow(row);
		}
	}
	
	private static final TranslatorTable TRANSLATORS = new TranslatorTable();
	private static class TranslatorTable extends Table {
		public TranslatorTable() {
			super("Liblouis Translators", Table.MASK_FOR_GENERIC_LASTING_EVENTS);
		}
		public final StringColumn TABLE = new StringColumn("Table");
		private final ObjectRowIndexMap rows = new ObjectRowIndexMap();
		public void createRow(Object translator, String table) {
			int row = createRow();
			rows.put(translator, row);
			TABLE.setValue(row, table);
		}
		public int getRow(Object translator) {
			return rows.get(translator);
		}
	}
	
	private static final TranslationTable TRANSLATIONS = new TranslationTable();
	private static class TranslationTable extends Table {
		public TranslationTable() {
			super(TRANSLATORS, "Translations", Table.MASK_FOR_SINGLE_METHOD_CALL_LASTING_EVENTS);
		}
		private final StringColumn INPUT = new StringColumn("Input");
		public int createRow(Object translator, String input) {
			int row = createRow(TRANSLATORS.getRow(translator));
			INPUT.setValue(row, input);
			return row;
		}
	}
}
