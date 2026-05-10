from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import cm
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
from reportlab.platypus import (SimpleDocTemplate, Paragraph, Spacer, Table,
                                TableStyle, HRFlowable, PageBreak, KeepTogether)
from reportlab.graphics.shapes import (Drawing, Rect, Circle, Line, String,
                                       Polygon, Ellipse, RoundRect)
from reportlab.graphics import renderPDF

W, H = A4

# ── Palette ──────────────────────────────────────────────────────────────────
DARK_BLUE   = colors.HexColor("#0d1b4b")
MID_BLUE    = colors.HexColor("#1565c0")
LIGHT_BLUE  = colors.HexColor("#e3f2fd")
ACCENT      = colors.HexColor("#0288d1")
TEAL        = colors.HexColor("#00695c")
LIGHT_TEAL  = colors.HexColor("#e0f2f1")
PURPLE      = colors.HexColor("#4a148c")
LIGHT_PURP  = colors.HexColor("#f3e5f5")
GREEN       = colors.HexColor("#1b5e20")
LIGHT_GREEN = colors.HexColor("#e8f5e9")
ORANGE      = colors.HexColor("#e65100")
LIGHT_ORG   = colors.HexColor("#fff3e0")
RED         = colors.HexColor("#b71c1c")
LIGHT_RED   = colors.HexColor("#ffebee")
YELLOW      = colors.HexColor("#f9a825")
LIGHT_YEL   = colors.HexColor("#fffde7")
GREY_BG     = colors.HexColor("#f5f5f5")
BORDER      = colors.HexColor("#bdbdbd")
DARK_TEXT   = colors.HexColor("#212121")
WHITE       = colors.white

# ── Style factory ─────────────────────────────────────────────────────────────
def S(name, **kw):
    return ParagraphStyle(name, **kw)

cov_title = S("CT", fontSize=26, textColor=WHITE, fontName="Helvetica-Bold",
              alignment=TA_CENTER, leading=32)
cov_sub   = S("CS", fontSize=13, textColor=colors.HexColor("#b3e5fc"),
              fontName="Helvetica", alignment=TA_CENTER, leading=18)
cov_pts   = S("CP", fontSize=11, textColor=colors.HexColor("#fff9c4"),
              fontName="Helvetica", alignment=TA_CENTER, leading=20)

ch_head   = S("CH", fontSize=16, textColor=WHITE, fontName="Helvetica-Bold",
              alignment=TA_CENTER, leading=20)
sec_head  = S("SH", fontSize=12, textColor=DARK_BLUE, fontName="Helvetica-Bold",
              leading=15, spaceBefore=8, spaceAfter=3)
body      = S("BD", fontSize=10.5, textColor=DARK_TEXT, fontName="Helvetica",
              leading=15, alignment=TA_JUSTIFY)
bul       = S("BL", fontSize=10.5, textColor=DARK_TEXT, fontName="Helvetica",
              leading=14, leftIndent=14, bulletIndent=4)
rxn       = S("RX", fontSize=11, textColor=TEAL, fontName="Helvetica-Bold",
              leading=15, alignment=TA_CENTER, spaceBefore=3, spaceAfter=3)
tip_s     = S("TP", fontSize=10, textColor=GREEN, fontName="Helvetica-Bold",
              leading=13)
small     = S("SM", fontSize=9, textColor=colors.grey, fontName="Helvetica-Oblique",
              leading=12, alignment=TA_CENTER)
tbl_hd    = S("TH", fontSize=10, textColor=WHITE, fontName="Helvetica-Bold",
              leading=13, alignment=TA_CENTER)
tbl_bd    = S("TB", fontSize=9.5, textColor=DARK_TEXT, fontName="Helvetica",
              leading=13)

# ── Helpers ───────────────────────────────────────────────────────────────────
def sp(h=6):  return Spacer(1, h)
def hr(c=BORDER): return HRFlowable(width="100%", thickness=0.6, color=c,
                                     spaceAfter=5, spaceBefore=5)

def banner(text, bg=DARK_BLUE):
    t = Table([[Paragraph(text, ch_head)]], colWidths=[17*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), bg),
        ("TOPPADDING",    (0,0),(-1,-1), 10),
        ("BOTTOMPADDING", (0,0),(-1,-1), 10),
        ("LEFTPADDING",   (0,0),(-1,-1), 12),
        ("RIGHTPADDING",  (0,0),(-1,-1), 12),
    ]))
    return t

def box(content_para, bg=LIGHT_BLUE, border_c=MID_BLUE):
    t = Table([[content_para]], colWidths=[17*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), bg),
        ("BOX",           (0,0),(-1,-1), 1.2, border_c),
        ("TOPPADDING",    (0,0),(-1,-1), 7),
        ("BOTTOMPADDING", (0,0),(-1,-1), 7),
        ("LEFTPADDING",   (0,0),(-1,-1), 10),
        ("RIGHTPADDING",  (0,0),(-1,-1), 10),
    ]))
    return t

def infobox(text, bg=LIGHT_BLUE, bc=MID_BLUE):
    return box(Paragraph(text, body), bg, bc)

def rxnbox(lines):
    rows = [[Paragraph(l, rxn)] for l in lines]
    t = Table(rows, colWidths=[17*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), LIGHT_TEAL),
        ("BOX",           (0,0),(-1,-1), 1.5, TEAL),
        ("INNERGRID",     (0,0),(-1,-1), 0.4, colors.HexColor("#80cbc4")),
        ("TOPPADDING",    (0,0),(-1,-1), 5),
        ("BOTTOMPADDING", (0,0),(-1,-1), 5),
        ("LEFTPADDING",   (0,0),(-1,-1), 8),
        ("RIGHTPADDING",  (0,0),(-1,-1), 8),
    ]))
    return t

def tipbox(text):
    t = Table([[Paragraph("★  EXAM TIP: " + text, tip_s)]], colWidths=[17*cm])
    t.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), LIGHT_YEL),
        ("BOX",           (0,0),(-1,-1), 1.2, YELLOW),
        ("TOPPADDING",    (0,0),(-1,-1), 7),
        ("BOTTOMPADDING", (0,0),(-1,-1), 7),
        ("LEFTPADDING",   (0,0),(-1,-1), 10),
        ("RIGHTPADDING",  (0,0),(-1,-1), 10),
    ]))
    return t

def grid_table(data, col_widths, header_bg=DARK_BLUE, row_colors=None):
    if row_colors is None:
        row_colors = [GREY_BG, WHITE]
    t = Table(data, colWidths=col_widths)
    style = [
        ("BACKGROUND",    (0,0),(-1, 0), header_bg),
        ("TEXTCOLOR",     (0,0),(-1, 0), WHITE),
        ("FONTNAME",      (0,0),(-1, 0), "Helvetica-Bold"),
        ("ROWBACKGROUNDS",(0,1),(-1,-1), row_colors),
        ("BOX",           (0,0),(-1,-1), 1,   BORDER),
        ("INNERGRID",     (0,0),(-1,-1), 0.5, BORDER),
        ("TOPPADDING",    (0,0),(-1,-1), 6),
        ("BOTTOMPADDING", (0,0),(-1,-1), 6),
        ("LEFTPADDING",   (0,0),(-1,-1), 7),
        ("RIGHTPADDING",  (0,0),(-1,-1), 7),
        ("FONTSIZE",      (0,0),(-1,-1), 9.5),
        ("VALIGN",        (0,0),(-1,-1), "MIDDLE"),
    ]
    t.setStyle(TableStyle(style))
    return t

# ── Diagrams ──────────────────────────────────────────────────────────────────

def grignard_diagram():
    """Simple flow diagram for Grignard reagent preparation & reactions"""
    d = Drawing(480, 220)
    # Center: Grignard reagent box
    d.add(RoundRect(170, 85, 140, 50, 8, fillColor=colors.HexColor("#e8eaf6"),
                    strokeColor=PURPLE, strokeWidth=2))
    d.add(String(240, 116, "Grignard Reagent", fontSize=10,
                 fillColor=PURPLE, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(240, 103, "R-Mg-X", fontSize=12,
                 fillColor=PURPLE, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(240, 91, "(in dry ether)", fontSize=8,
                 fillColor=colors.grey, textAnchor="middle"))

    # Left: preparation
    d.add(Rect(18, 90, 110, 40, fillColor=LIGHT_GREEN, strokeColor=GREEN, strokeWidth=1.5))
    d.add(String(73, 116, "R-X (Alkyl Halide)", fontSize=8.5,
                 fillColor=GREEN, textAnchor="middle"))
    d.add(String(73, 104, "+ Mg metal", fontSize=8.5,
                 fillColor=GREEN, textAnchor="middle"))
    d.add(String(73, 93, "(dry ether solvent)", fontSize=7.5,
                 fillColor=colors.grey, textAnchor="middle"))
    # Arrow left to center
    d.add(Line(128, 110, 168, 110, strokeColor=GREEN, strokeWidth=2))
    d.add(Polygon([168,110,160,115,160,105], fillColor=GREEN, strokeColor=GREEN))
    d.add(String(148, 120, "Prepare", fontSize=7.5, fillColor=GREEN, textAnchor="middle"))

    # Top products (4 arrows going up/out)
    products = [
        (60,  185, "1° Alcohol\n(from HCHO)", MID_BLUE),
        (165, 190, "2° Alcohol\n(from RCHO)", TEAL),
        (285, 190, "3° Alcohol\n(from R\u2082CO)", ORANGE),
        (400, 185, "Carboxylic Acid\n(from CO\u2082)", RED),
    ]
    for px, py, label, col in products:
        d.add(Rect(px-45, py-20, 92, 36, fillColor=colors.HexColor("#fafafa"),
                   strokeColor=col, strokeWidth=1.5))
        lines = label.split("\n")
        d.add(String(px, py+4, lines[0], fontSize=8, fillColor=col,
                     textAnchor="middle", fontName="Helvetica-Bold"))
        d.add(String(px, py-8, lines[1], fontSize=7.5, fillColor=DARK_TEXT,
                     textAnchor="middle"))
        # Arrow from center box to product
        d.add(Line(240, 135, px, py-20, strokeColor=col, strokeWidth=1.2))
        d.add(Polygon([px, py-20, px-5, py-12, px+5, py-12],
                      fillColor=col, strokeColor=col))

    d.add(String(240, 12, "Fig: Grignard Reagent — Preparation and Key Reactions",
                 fontSize=9, fillColor=colors.grey, textAnchor="middle",
                 fontName="Helvetica-Oblique"))
    return d


def polymer_types_diagram():
    """Simple diagram showing polymer classification tree"""
    d = Drawing(480, 200)
    # Root
    d.add(RoundRect(180, 155, 120, 35, 6,
                    fillColor=DARK_BLUE, strokeColor=DARK_BLUE))
    d.add(String(240, 175, "POLYMERS", fontSize=11, fillColor=WHITE,
                 textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(240, 162, "(Classification)", fontSize=8.5, fillColor=colors.HexColor("#bbdefb"),
                 textAnchor="middle"))

    # Branches: 5 categories
    cats = [
        (40,  95, "By\nSource",     colors.HexColor("#1565c0"), "Natural\nSynthetic\nSemi-synth."),
        (130, 95, "By\nStructure",  TEAL,   "Linear\nBranched\nCross-linked"),
        (240, 95, "By\nThermal",    ORANGE, "Thermo-\nplastic\nThermosetting"),
        (350, 95, "Conducting",     PURPLE, "e.g.\nPANI\nPolythioph."),
        (440, 95, "Biodegr-\nadable", GREEN, "e.g.\nPLA\nPHA"),
    ]
    for cx, cy, title, col, sub in cats:
        d.add(RoundRect(cx-38, cy, 76, 50, 5,
                        fillColor=colors.HexColor("#fafafa"), strokeColor=col, strokeWidth=1.5))
        t_lines = title.split("\n")
        d.add(String(cx, cy+42, t_lines[0], fontSize=8.5, fillColor=col,
                     textAnchor="middle", fontName="Helvetica-Bold"))
        if len(t_lines) > 1:
            d.add(String(cx, cy+31, t_lines[1], fontSize=8.5, fillColor=col,
                         textAnchor="middle", fontName="Helvetica-Bold"))
        for i, sl in enumerate(sub.split("\n")):
            d.add(String(cx, cy+19-i*10, sl, fontSize=7.5, fillColor=DARK_TEXT,
                         textAnchor="middle"))
        # Arrow from root
        d.add(Line(240, 155, cx, cy+50, strokeColor=col, strokeWidth=1.2))

    d.add(String(240, 12, "Fig: Polymer Classification Overview",
                 fontSize=9, fillColor=colors.grey, textAnchor="middle",
                 fontName="Helvetica-Oblique"))
    return d


def polymerization_diagram():
    """Addition vs Condensation simple visual"""
    d = Drawing(480, 160)
    # Addition side
    d.add(Rect(10, 50, 200, 90, fillColor=LIGHT_BLUE,
               strokeColor=MID_BLUE, strokeWidth=1.5, rx=6))
    d.add(String(110, 128, "ADDITION POLYMERIZATION", fontSize=9,
                 fillColor=MID_BLUE, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(110, 114, "Monomer: has C=C double bond", fontSize=8.5,
                 fillColor=DARK_TEXT, textAnchor="middle"))
    d.add(String(110, 101, "n CH2=CH2  ---->  (-CH2-CH2-)n", fontSize=8.5,
                 fillColor=TEAL, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(110, 88, "(Ethylene --> Polyethylene)", fontSize=8,
                 fillColor=colors.grey, textAnchor="middle"))
    d.add(String(110, 73, "No by-product released", fontSize=8.5,
                 fillColor=GREEN, textAnchor="middle"))
    d.add(String(110, 62, "Polymer = sum of all monomers", fontSize=8,
                 fillColor=DARK_TEXT, textAnchor="middle"))

    # Condensation side
    d.add(Rect(270, 50, 200, 90, fillColor=LIGHT_ORG,
               strokeColor=ORANGE, strokeWidth=1.5, rx=6))
    d.add(String(370, 128, "CONDENSATION POLYMERIZATION", fontSize=9,
                 fillColor=ORANGE, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(370, 114, "Monomers: 2 different functional groups", fontSize=8.5,
                 fillColor=DARK_TEXT, textAnchor="middle"))
    d.add(String(370, 101, "Diacid + Diamine  ---->  Nylon + H2O", fontSize=8.5,
                 fillColor=TEAL, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(370, 88, "(Hexanedioic acid + Hexamethylene diamine)", fontSize=7.5,
                 fillColor=colors.grey, textAnchor="middle"))
    d.add(String(370, 73, "By-product released (H2O or HCl)", fontSize=8.5,
                 fillColor=RED, textAnchor="middle"))
    d.add(String(370, 62, "Polymer < sum of monomers", fontSize=8,
                 fillColor=DARK_TEXT, textAnchor="middle"))

    # VS label
    d.add(Circle(240, 95, 20, fillColor=DARK_BLUE, strokeColor=DARK_BLUE))
    d.add(String(240, 91, "VS", fontSize=11, fillColor=WHITE,
                 textAnchor="middle", fontName="Helvetica-Bold"))

    d.add(String(240, 12, "Fig: Addition vs Condensation Polymerization",
                 fontSize=9, fillColor=colors.grey, textAnchor="middle",
                 fontName="Helvetica-Oblique"))
    return d


def conducting_polymer_diagram():
    """Polyaniline doping concept"""
    d = Drawing(480, 140)
    # Emeraldine base (non-conducting)
    d.add(Rect(20, 80, 180, 45, fillColor=GREY_BG, strokeColor=BORDER, strokeWidth=1.5, rx=4))
    d.add(String(110, 112, "Polyaniline (Emeraldine Base)", fontSize=8.5,
                 fillColor=DARK_TEXT, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(110, 100, "Non-conducting form", fontSize=8,
                 fillColor=colors.grey, textAnchor="middle"))
    d.add(String(110, 88, "(-NH-C6H4-N=C6H4=)n", fontSize=8.5,
                 fillColor=PURPLE, textAnchor="middle"))

    # Arrow with doping
    d.add(Line(200, 102, 265, 102, strokeColor=GREEN, strokeWidth=2))
    d.add(Polygon([265,102,256,107,256,97], fillColor=GREEN, strokeColor=GREEN))
    d.add(String(232, 115, "DOPING", fontSize=8.5, fillColor=GREEN,
                 textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(232, 104, "(HCl or", fontSize=7.5, fillColor=GREEN, textAnchor="middle"))
    d.add(String(232, 94, "H2SO4)", fontSize=7.5, fillColor=GREEN, textAnchor="middle"))

    # Emeraldine salt (conducting)
    d.add(Rect(268, 80, 190, 45, fillColor=LIGHT_GREEN, strokeColor=GREEN, strokeWidth=2, rx=4))
    d.add(String(363, 112, "Emeraldine Salt (PANI-H+)", fontSize=8.5,
                 fillColor=GREEN, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(363, 100, "CONDUCTING form", fontSize=8,
                 fillColor=DARK_TEXT, textAnchor="middle", fontName="Helvetica-Bold"))
    d.add(String(363, 88, "Conductivity: 1-100 S/cm", fontSize=8.5,
                 fillColor=TEAL, textAnchor="middle"))

    d.add(String(240, 68, "Mechanism: Doping creates mobile charge carriers (polarons/bipolarons)",
                 fontSize=8, fillColor=DARK_TEXT, textAnchor="middle"))

    d.add(String(240, 12, "Fig: Polyaniline (PANI) — Doping to achieve Conductivity",
                 fontSize=9, fillColor=colors.grey, textAnchor="middle",
                 fontName="Helvetica-Oblique"))
    return d


# ── PDF Builder ───────────────────────────────────────────────────────────────
def build():
    out = "/mnt/user-data/outputs/Engineering_Chemistry_Unit5_Notes.pdf"
    doc = SimpleDocTemplate(out, pagesize=A4,
                            leftMargin=2*cm, rightMargin=2*cm,
                            topMargin=2*cm, bottomMargin=2*cm)
    story = []

    # ── COVER ──────────────────────────────────────────────────────────────
    cover = Table([
        [Paragraph("Engineering Chemistry  |  BAS102 / BAS202", cov_sub)],
        [sp(4)],
        [Paragraph("UNIT 5 — Complete Exam Notes", cov_title)],
        [sp(6)],
        [Paragraph(
            "Organometallic Compounds  •  Polymers & Types\n"
            "Conducting Polymers  •  Biodegradable Polymers\n"
            "Grignard Reagent  •  All Reactions & Diagrams",
            cov_pts)],
        [sp(10)],
        [Paragraph("Simple Language  |  AKTU End Semester Exam Ready", cov_sub)],
    ], colWidths=[17*cm])
    cover.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), DARK_BLUE),
        ("TOPPADDING",    (0,0),(-1,-1), 8),
        ("BOTTOMPADDING", (0,0),(-1,-1), 8),
        ("LEFTPADDING",   (0,0),(-1,-1), 18),
        ("RIGHTPADDING",  (0,0),(-1,-1), 18),
    ]))
    story += [cover, PageBreak()]

    # ════════════════════════════════════════════════════════════════════════
    # TOPIC 1 — ORGANOMETALLIC COMPOUNDS
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("⚗️  Topic 1: Organometallic Compounds", DARK_BLUE))
    story.append(sp(10))

    story.append(Paragraph("What are Organometallic Compounds?", sec_head))
    story.append(infobox(
        "<b>Definition:</b> Organometallic compounds are those compounds in which there is "
        "a <b>direct bond between a Carbon (C) atom and a Metal atom</b>.<br/><br/>"
        "Simple words mein: Yeh compounds jisme ek side organic group (carbon-containing) "
        "hota hai aur doosri side metal hoti hai — dono directly bonded hote hain.<br/><br/>"
        "<b>General Formula:</b>  R — M  &nbsp;&nbsp; (R = organic group, M = metal)"
    ))
    story.append(sp(8))

    story.append(Paragraph("Examples of Organometallic Compounds", sec_head))
    ex_data = [
        [Paragraph("<b>Compound</b>", tbl_hd), Paragraph("<b>Formula</b>", tbl_hd),
         Paragraph("<b>Metal Involved</b>", tbl_hd), Paragraph("<b>Type</b>", tbl_hd)],
        ["Grignard Reagent",    "R-Mg-X",              "Magnesium (Mg)", "Most important in AKTU"],
        ["Tetraethyl Lead",     "Pb(C2H5)4",           "Lead (Pb)",      "Antiknock agent in petrol"],
        ["Ferrocene",           "Fe(C5H5)2",           "Iron (Fe)",      "Sandwich compound"],
        ["Zeise's Salt",        "K[PtCl3(C2H4)]",     "Platinum (Pt)", "First organometallic"],
        ["Dimethyl Zinc",       "Zn(CH3)2",            "Zinc (Zn)",      "Organozinc compound"],
        ["LiAlH4",              "LiAlH4",              "Li + Al",        "Reducing agent"],
    ]
    story.append(grid_table(ex_data, [4*cm, 3.5*cm, 4*cm, 5.5*cm], DARK_BLUE))
    story.append(sp(8))

    story.append(Paragraph("General Methods of Preparation", sec_head))
    methods = [
        ("<b>Method 1 — Direct reaction of metal with organic halide:</b><br/>"
         "2R-X  +  2Na  →  R-R  +  2NaX  &nbsp;&nbsp;&nbsp; (Wurtz reaction)<br/>"
         "R-X  +  Mg  →  R-Mg-X  &nbsp;&nbsp;&nbsp; (Grignard formation — in dry ether)"),
        ("<b>Method 2 — Reaction of metal with organometallic compound (Transmetallation):</b><br/>"
         "R-Mg-X  +  LiX  →  R-Li  +  MgX<sub>2</sub>"),
        ("<b>Method 3 — Reaction with metal hydrides:</b><br/>"
         "Metal hydride + organic compound → organometallic<br/>"
         "Example: LiH  +  AlCl<sub>3</sub>  →  LiAlH<sub>4</sub>"),
    ]
    for i, m in enumerate(methods):
        story.append(infobox(m, bg=LIGHT_BLUE if i%2==0 else LIGHT_GREEN,
                             bc=MID_BLUE if i%2==0 else GREEN))
        story.append(sp(4))

    story.append(sp(6))
    story.append(Paragraph("Industrial Applications of Organometallic Compounds", sec_head))
    apps = [
        "• <b>Catalysis:</b> Ziegler-Natta catalyst (TiCl<sub>4</sub> + Al(C<sub>2</sub>H<sub>5</sub>)<sub>3</sub>) used in making polyethylene and polypropylene.",
        "• <b>Antiknock agent:</b> Tetraethyl lead — Pb(C<sub>2</sub>H<sub>5</sub>)<sub>4</sub> — added to petrol to prevent engine knocking.",
        "• <b>Organic synthesis:</b> Grignard reagent is used to make alcohols, acids, ketones.",
        "• <b>Reducing agents:</b> LiAlH<sub>4</sub> used to reduce aldehydes, ketones, acids.",
        "• <b>Medicine:</b> Cisplatin (Pt-based compound) — used as anticancer drug.",
        "• <b>Semiconductor industry:</b> Organometallic CVD (MOCVD) used to deposit thin films.",
    ]
    for a in apps:
        story.append(Paragraph(a, bul))
    story.append(sp(6))
    story.append(tipbox(
        "Definition + 2 examples + preparation method + applications — yeh 4 points "
        "likhne se 5-mark question easily cover ho jaata hai!"
    ))
    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # TOPIC 2 — POLYMERS
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("🔗  Topic 2: Polymers — Introduction & Classification", MID_BLUE))
    story.append(sp(10))

    story.append(Paragraph("What is a Polymer?", sec_head))
    story.append(infobox(
        "<b>Polymer</b> (Greek: poly = many, meros = units) — A very large molecule "
        "made by joining thousands of small repeating units called <b>monomers</b>.<br/><br/>"
        "<b>Monomer</b> = Small molecule that repeats<br/>"
        "<b>Polymer</b> = Big chain of monomers joined together<br/><br/>"
        "<b>Example:</b>  Ethylene (CH<sub>2</sub>=CH<sub>2</sub>) — monomer  "
        "→  Polyethylene — polymer (plastic bags)<br/>"
        "<b>Degree of Polymerization (n):</b> Number of monomer units in one polymer chain."
    ))
    story.append(sp(8))

    story.append(Paragraph("Classification of Polymers", sec_head))
    story.append(polymer_types_diagram())
    story.append(sp(8))

    # Classification table
    cls_data = [
        [Paragraph("<b>Basis</b>", tbl_hd), Paragraph("<b>Type</b>", tbl_hd),
         Paragraph("<b>Examples</b>", tbl_hd)],
        ["By Source", "Natural", "Natural rubber, Starch, Cellulose, Silk, Wool"],
        ["By Source", "Synthetic", "Nylon, Teflon, Bakelite, PVC, Polythene"],
        ["By Source", "Semi-synthetic", "Rayon (from cellulose), Cellulose acetate"],
        ["By Structure", "Linear", "PVC, Nylon, HDPE — straight long chain"],
        ["By Structure", "Branched", "LDPE — branches on main chain"],
        ["By Structure", "Cross-linked", "Bakelite, Vulcanized rubber — 3D network"],
        ["By Thermal Behaviour", "Thermoplastic", "PVC, Nylon, Polythene — soften on heating"],
        ["By Thermal Behaviour", "Thermosetting", "Bakelite, Urea-formaldehyde — harden on heating"],
        ["By Polymerization", "Addition", "Teflon, PVC, Polyethylene"],
        ["By Polymerization", "Condensation", "Nylon, Dacron, Bakelite"],
    ]
    story.append(grid_table(cls_data, [4*cm, 4*cm, 9*cm], MID_BLUE))
    story.append(sp(8))

    story.append(Paragraph("Polymerization Processes", sec_head))
    story.append(polymerization_diagram())
    story.append(sp(8))

    story.append(Paragraph("Addition Polymerization — Step by Step", sec_head))
    story.append(infobox(
        "<b>Free Radical Mechanism</b> has 3 steps:<br/><br/>"
        "<b>Step 1 — Initiation:</b> Initiator (e.g. benzoyl peroxide) breaks into free radicals "
        "by heat/UV. Free radical attacks monomer C=C bond.<br/>"
        "R•  +  CH<sub>2</sub>=CH<sub>2</sub>  →  R-CH<sub>2</sub>-CH<sub>2</sub>•<br/><br/>"
        "<b>Step 2 — Propagation:</b> The growing radical keeps adding monomer units, chain grows.<br/>"
        "R-CH<sub>2</sub>-CH<sub>2</sub>•  +  CH<sub>2</sub>=CH<sub>2</sub>  →  R-(CH<sub>2</sub>-CH<sub>2</sub>)<sub>2</sub>•  → ... keeps growing<br/><br/>"
        "<b>Step 3 — Termination:</b> Two radicals combine — chain stops growing.<br/>"
        "2 R-(CH<sub>2</sub>-CH<sub>2</sub>)<sub>n</sub>•  →  R-(CH<sub>2</sub>-CH<sub>2</sub>)<sub>2n</sub>-R  (Polymer formed)"
    ))
    story.append(sp(6))

    story.append(Paragraph("Condensation Polymerization — Key Points", sec_head))
    story.append(infobox(
        "• Two monomers with <b>different functional groups</b> (-NH<sub>2</sub>, -COOH, -OH) react.<br/>"
        "• A small molecule is <b>released as by-product</b> (H<sub>2</sub>O, HCl, NH<sub>3</sub>).<br/>"
        "• No need for double bond in monomer.<br/>"
        "• <b>Examples:</b> Nylon 6,6 (releases H<sub>2</sub>O), Dacron (releases H<sub>2</sub>O), "
        "Bakelite (releases H<sub>2</sub>O).",
        bg=LIGHT_ORG, bc=ORANGE
    ))
    story.append(sp(6))

    story.append(Paragraph("Thermoplastic vs Thermosetting Polymers", sec_head))
    tt_data = [
        [Paragraph("<b>Property</b>", tbl_hd), Paragraph("<b>Thermoplastic</b>", tbl_hd),
         Paragraph("<b>Thermosetting</b>", tbl_hd)],
        ["On heating",      "Softens (can remould)",        "Hardens permanently"],
        ["Structure",       "Linear / branched chains",     "3D cross-linked network"],
        ["Recyclable?",     "Yes — can melt & reshape",     "No — once set, cannot remelt"],
        ["Solubility",      "Soluble in some solvents",     "Insoluble"],
        ["Strength",        "Less strong",                  "Very strong & rigid"],
        ["Examples",        "PVC, Nylon, Polythene, Teflon","Bakelite, Urea-formaldehyde, Epoxy"],
        ["Uses",            "Pipes, bottles, films",        "Electrical fittings, adhesives"],
    ]
    story.append(grid_table(tt_data, [4*cm, 6.5*cm, 6.5*cm], TEAL,
                             [LIGHT_TEAL, WHITE]))
    story.append(sp(6))
    story.append(tipbox(
        "Thermoplastic vs Thermosetting comparison table is a VERY common 5-mark question. "
        "Remember: Thermo<b>plastic</b> = can be remoulded (like plastic bags). "
        "Thermo<b>setting</b> = sets permanently (like Bakelite switch boards)."
    ))
    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # TOPIC 3 — CONDUCTING POLYMERS
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("⚡  Topic 3: Conducting Polymers", PURPLE))
    story.append(sp(10))

    story.append(Paragraph("What are Conducting Polymers?", sec_head))
    story.append(infobox(
        "Normally, polymers are <b>insulators</b> (they do not conduct electricity). "
        "But some special polymers, when treated with certain chemicals (<b>doping</b>), "
        "can conduct electricity. These are called <b>Conducting Polymers</b> or "
        "<b>Synthetic Metals</b>.<br/><br/>"
        "<b>Discovery:</b> Alan J. Heeger, Alan G. MacDiarmid, and Hideki Shirakawa discovered "
        "conducting polymers in 1977 — they won the <b>Nobel Prize in Chemistry (2000)</b>.",
        bg=LIGHT_PURP, bc=PURPLE
    ))
    story.append(sp(8))

    story.append(Paragraph("Why do they Conduct? — The Concept of Doping", sec_head))
    story.append(infobox(
        "Conducting polymers have a <b>conjugated system</b> — alternating single and double "
        "bonds (C=C-C=C-...). This creates a <b>delocalized pi-electron system</b>.<br/><br/>"
        "But pure polymer still doesn't conduct well. When we add a dopant (oxidizing or "
        "reducing agent), it creates <b>charge carriers (polarons/bipolarons)</b> that can move "
        "along the chain — just like electrons in metals.<br/><br/>"
        "• <b>p-doping</b> (oxidation): Remove electrons → positive charge carriers<br/>"
        "• <b>n-doping</b> (reduction): Add electrons → negative charge carriers"
    ))
    story.append(sp(8))

    story.append(conducting_polymer_diagram())
    story.append(sp(8))

    story.append(Paragraph("Important Conducting Polymers", sec_head))
    cp_data = [
        [Paragraph("<b>Polymer</b>", tbl_hd), Paragraph("<b>Abbreviation</b>", tbl_hd),
         Paragraph("<b>Dopant Used</b>", tbl_hd), Paragraph("<b>Conductivity</b>", tbl_hd)],
        ["Polyacetylene",          "PA",   "I2, AsF5",       "~10^3 - 10^5 S/cm (highest)"],
        ["Polyaniline",            "PANI", "HCl, H2SO4",     "1-100 S/cm (most studied)"],
        ["Polythiophene",          "PT",   "FeCl3",          "~10^3 S/cm"],
        ["Polypyrrole",            "PPy",  "BF4-, ClO4-",    "10-100 S/cm"],
        ["Poly-p-phenylene",       "PPP",  "AsF5, K",        "~500 S/cm"],
    ]
    story.append(grid_table(cp_data, [4.5*cm, 3*cm, 3.5*cm, 6*cm], PURPLE,
                             [LIGHT_PURP, WHITE]))
    story.append(sp(8))

    story.append(Paragraph("Polyaniline (PANI) — Most Important for AKTU", sec_head))
    story.append(rxnbox([
        "Synthesis:  n C6H5-NH2  +  [Oxidant (APS)]  -->  (-C6H4-NH-)n  (Polyaniline)",
        "Doping:     PANI (base)  +  HCl  -->  PANI-H+ Cl-  (Conducting Emeraldine Salt)",
        "Dedoping:   PANI-H+ Cl-  +  NaOH  -->  PANI (base)  +  NaCl  +  H2O",
    ]))
    story.append(sp(8))

    story.append(Paragraph("Applications of Conducting Polymers", sec_head))
    cap_list = [
        "• <b>Rechargeable batteries:</b> Polythiophene and PANI used as electrode materials.",
        "• <b>Solar cells:</b> Light-to-electricity conversion using conjugated polymers.",
        "• <b>LED displays:</b> Polymer LEDs (PLEDs) used in flexible displays.",
        "• <b>Corrosion protection:</b> PANI coated on metals to prevent rust.",
        "• <b>Biosensors:</b> Detect glucose, DNA, and other biomolecules.",
        "• <b>Electromagnetic shielding:</b> Protect electronic devices from interference.",
        "• <b>Supercapacitors:</b> Energy storage devices with fast charge/discharge.",
    ]
    for c in cap_list:
        story.append(Paragraph(c, bul))
    story.append(sp(6))
    story.append(tipbox(
        "Nobel Prize year (2000) + definition of doping + PANI example + "
        "3-4 applications — yeh sab likhne se 10-mark question full marks aayega!"
    ))
    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # TOPIC 4 — BIODEGRADABLE POLYMERS
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("🌿  Topic 4: Biodegradable Polymers", GREEN))
    story.append(sp(10))

    story.append(Paragraph("What are Biodegradable Polymers?", sec_head))
    story.append(infobox(
        "<b>Biodegradable polymers</b> are those polymers which can be broken down naturally "
        "by microorganisms (bacteria, fungi) in the environment into simple, harmless "
        "products like CO<sub>2</sub>, H<sub>2</sub>O, and biomass.<br/><br/>"
        "Simple words mein: Yeh polymers <b>nature mein apne aap khud gal jaate hain</b> — "
        "environment ko nuksan nahi hota.<br/><br/>"
        "<b>Why needed?</b> Regular plastics (PVC, Polyethylene) take <b>100-500 years</b> "
        "to degrade — causing huge pollution. Biodegradable polymers solve this problem.",
        bg=LIGHT_GREEN, bc=GREEN
    ))
    story.append(sp(8))

    story.append(Paragraph("Important Examples of Biodegradable Polymers", sec_head))
    bio_data = [
        [Paragraph("<b>Polymer</b>", tbl_hd), Paragraph("<b>Full Name</b>", tbl_hd),
         Paragraph("<b>Monomer Source</b>", tbl_hd), Paragraph("<b>Uses</b>", tbl_hd)],
        ["PLA",  "Polylactic Acid",          "Lactic acid (from corn/sugarcane)", "Packaging, medical implants, 3D printing"],
        ["PHA",  "Polyhydroxyalkanoates",    "Bacteria-produced (microbial)",    "Medical sutures, drug delivery"],
        ["PGA",  "Polyglycolic Acid",        "Glycolic acid",                    "Surgical sutures, tissue engineering"],
        ["PCL",  "Polycaprolactone",         "Caprolactone monomer",             "Drug delivery, scaffold in bone repair"],
        ["PHBV", "Polyhydroxybutyrate-val.", "Bacterial fermentation",           "Packaging, agricultural films"],
    ]
    story.append(grid_table(bio_data, [1.8*cm, 4.2*cm, 4.5*cm, 6.5*cm], GREEN,
                             [LIGHT_GREEN, WHITE]))
    story.append(sp(8))

    story.append(Paragraph("PLA — Polylactic Acid (Most Important Example)", sec_head))
    story.append(rxnbox([
        "Monomer: Lactic Acid  [CH3-CH(OH)-COOH]  (from corn starch / sugarcane)",
        "Polymerization: n CH3-CH(OH)-COOH  -->  (-O-CH(CH3)-CO-)n  +  n H2O",
        "Degradation: PLA + H2O  + microbes  -->  Lactic acid  -->  CO2  +  H2O",
    ]))
    story.append(sp(8))

    story.append(Paragraph("Properties of Biodegradable Polymers", sec_head))
    props = [
        "• They are <b>eco-friendly</b> — do not accumulate in soil or water.",
        "• Degradation time: <b>6 months to 2 years</b> (vs 500 years for regular plastics).",
        "• Products of degradation: <b>CO<sub>2</sub>, H<sub>2</sub>O, methane, biomass</b> — all harmless.",
        "• Most are <b>biocompatible</b> — safe for use inside the human body.",
        "• Usually made from <b>renewable resources</b> (plant sugars, bacteria) — sustainable.",
    ]
    for p in props:
        story.append(Paragraph(p, bul))
    story.append(sp(6))

    story.append(Paragraph("Applications", sec_head))
    bio_apps = [
        "• <b>Medical:</b> Surgical sutures (PGA, PLA), drug delivery capsules, bone screws, tissue scaffolds.",
        "• <b>Packaging:</b> Food containers, shopping bags, cups — replace plastic.",
        "• <b>Agriculture:</b> Mulch films, fertilizer coatings — degrade in soil after use.",
        "• <b>3D printing:</b> PLA filament is the most popular eco-friendly 3D printing material.",
        "• <b>Composting:</b> Compostable cutlery and plates from PLA.",
    ]
    for a in bio_apps:
        story.append(Paragraph(a, bul))
    story.append(sp(6))

    story.append(infobox(
        "<b>Difference: Biodegradable vs Non-biodegradable Polymer</b><br/><br/>"
        "<b>Biodegradable:</b> PLA, PHA, PGA — broken down by microbes — eco-friendly<br/>"
        "<b>Non-biodegradable:</b> PVC, Polythene, Nylon — stay in environment for centuries — cause pollution",
        bg=LIGHT_GREEN, bc=GREEN
    ))
    story.append(sp(6))
    story.append(tipbox(
        "PLA example + degradation reaction + 3 applications = 5-mark answer ready! "
        "Biodegradable vs Non-biodegradable comparison is a favourite 2-mark question."
    ))
    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # TOPIC 5 — GRIGNARD REAGENT
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("🧪  Topic 5: Grignard Reagent (RMgX)", TEAL))
    story.append(sp(10))

    story.append(Paragraph("What is Grignard Reagent?", sec_head))
    story.append(infobox(
        "<b>Grignard Reagent</b> is an organometallic compound with the general formula "
        "<b>R-Mg-X</b>, where:<br/>"
        "• R = organic (alkyl or aryl) group<br/>"
        "• Mg = Magnesium metal<br/>"
        "• X = Halogen (Cl, Br, or I)<br/><br/>"
        "It was discovered by <b>Victor Grignard (1900)</b>, who received the Nobel Prize "
        "in Chemistry in 1912 for this discovery.<br/><br/>"
        "<b>Examples:</b> CH<sub>3</sub>MgBr (Methylmagnesium bromide), "
        "C<sub>2</sub>H<sub>5</sub>MgCl (Ethylmagnesium chloride), "
        "C<sub>6</sub>H<sub>5</sub>MgBr (Phenylmagnesium bromide)",
        bg=LIGHT_TEAL, bc=TEAL
    ))
    story.append(sp(8))

    story.append(Paragraph("Preparation of Grignard Reagent", sec_head))
    story.append(infobox(
        "<b>Method:</b> Alkyl halide (R-X) is treated with Magnesium metal in <b>dry ether</b> "
        "(anhydrous diethyl ether) as solvent.<br/><br/>"
        "<b>Important conditions:</b><br/>"
        "1. Solvent must be <b>completely dry</b> (anhydrous) — even a trace of water "
        "destroys Grignard reagent.<br/>"
        "2. Reaction done in <b>absence of air/oxygen</b> (inert atmosphere).<br/>"
        "3. Reactivity order of R-X: RI &gt; RBr &gt; RCl (Iodide reacts fastest)"
    ))
    story.append(sp(6))
    story.append(rxnbox([
        "R-X  +  Mg  ---dry ether--->  R-Mg-X  (Grignard Reagent)",
        "CH3Br  +  Mg  --dry ether-->  CH3-Mg-Br  (Methylmagnesium bromide)",
        "C6H5Br  +  Mg  --dry ether-->  C6H5-Mg-Br  (Phenylmagnesium bromide)",
    ]))
    story.append(sp(10))

    story.append(Paragraph("Reactions of Grignard Reagent (Applications)", sec_head))
    story.append(grignard_diagram())
    story.append(sp(10))

    story.append(Paragraph("Reaction 1 — With Formaldehyde (HCHO) → Primary Alcohol", sec_head))
    story.append(rxnbox([
        "Step 1:  R-Mg-X  +  HCHO  -->  R-CH2-OMgX  (Intermediate)",
        "Step 2:  R-CH2-OMgX  +  H2O/H+  -->  R-CH2-OH  +  Mg(OH)X",
        "Product: PRIMARY ALCOHOL (1° alcohol)  — carbon chain increases by 1",
    ]))
    story.append(sp(6))

    story.append(Paragraph("Reaction 2 — With Aldehyde (RCHO) → Secondary Alcohol", sec_head))
    story.append(rxnbox([
        "R-Mg-X  +  R'-CHO  -->  [R-CH(R')-OMgX]  -->  H3O+  -->  R-CH(OH)-R'",
        "Product: SECONDARY ALCOHOL (2° alcohol)  — 2 different R groups on C-OH",
    ]))
    story.append(sp(6))

    story.append(Paragraph("Reaction 3 — With Ketone (R2CO) → Tertiary Alcohol", sec_head))
    story.append(rxnbox([
        "R-Mg-X  +  R'-CO-R''  -->  [R-C(R')(R'')-OMgX]  -->  H3O+  -->  R-C(OH)(R')(R'')",
        "Product: TERTIARY ALCOHOL (3° alcohol)  — 3 different R groups on C-OH",
    ]))
    story.append(sp(6))

    story.append(Paragraph("Reaction 4 — With CO2 → Carboxylic Acid", sec_head))
    story.append(rxnbox([
        "R-Mg-X  +  CO2  -->  R-COOMgX  (Intermediate)",
        "R-COOMgX  +  H2O/H+  -->  R-COOH  +  Mg(OH)X",
        "Product: CARBOXYLIC ACID  — carbon chain increases by 1",
    ]))
    story.append(sp(6))

    story.append(Paragraph("Reaction 5 — With Water (Hydrolysis)", sec_head))
    story.append(rxnbox([
        "R-Mg-X  +  H2O  -->  R-H  +  Mg(OH)X",
        "This is why Grignard reagent must be prepared in DRY ether — water destroys it!",
    ]))
    story.append(sp(6))

    story.append(Paragraph("Reaction 6 — With Ester → Tertiary Alcohol", sec_head))
    story.append(rxnbox([
        "2 R-Mg-X  +  R'-COOR''  -->  R2-C(OH)-R'  +  MgX2  +  R''OMgX",
        "Product: TERTIARY ALCOHOL (2 moles of Grignard used)",
    ]))
    story.append(sp(8))

    # Summary table
    story.append(Paragraph("Quick Revision — Grignard Reactions Summary", sec_head))
    gr_sum = [
        [Paragraph("<b>Reacts with</b>", tbl_hd), Paragraph("<b>Product</b>", tbl_hd),
         Paragraph("<b>Type</b>", tbl_hd)],
        ["HCHO (Formaldehyde)",  "Primary Alcohol (1°)",     "Chain +1 carbon"],
        ["RCHO (Aldehyde)",      "Secondary Alcohol (2°)",   "Chain +1 carbon"],
        ["R2CO (Ketone)",        "Tertiary Alcohol (3°)",    "Chain +1 carbon"],
        ["CO2",                  "Carboxylic Acid (-COOH)",  "Chain +1 carbon"],
        ["H2O",                  "Alkane (R-H)",             "Reagent destroyed"],
        ["Ester (RCOOR')",       "Tertiary Alcohol (3°)",    "2 moles Grignard used"],
        ["NH3",                  "Alkane + Mg(OH)X",         "Acts as acid"],
    ]
    story.append(grid_table(gr_sum, [5.5*cm, 6*cm, 5.5*cm], TEAL,
                             [LIGHT_TEAL, WHITE]))
    story.append(sp(8))

    story.append(infobox(
        "<b>Why is Grignard Reagent so important?</b><br/>"
        "R-Mg-X acts as a powerful <b>nucleophile</b> (it attacks electron-poor carbon atoms). "
        "The C-Mg bond is highly polar — carbon acts as carbanion (R<super>-</super>), "
        "making it extremely reactive. It is one of the most versatile reagents in "
        "organic chemistry — used to build complex molecules from simple ones.",
        bg=LIGHT_TEAL, bc=TEAL
    ))
    story.append(sp(6))
    story.append(tipbox(
        "Grignard is a 10-mark favourite! Write: Discovery + Nobel year (1912) + "
        "Preparation (conditions: dry ether, no moisture) + minimum 4 reactions with "
        "products. The alcohol type pattern (1°, 2°, 3°) is very easy to remember!"
    ))
    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # IMPORTANT POLYMERS — QUICK REFERENCE
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("📋  Important Polymers — Preparation & Uses (Quick Ref)", ORANGE))
    story.append(sp(10))

    polymers = [
        ("Nylon 6,6", MID_BLUE, LIGHT_BLUE,
         "Hexamethylene diamine  +  Adipic acid",
         "n H2N-(CH2)6-NH2  +  n HOOC-(CH2)4-COOH  -->  [-NH-(CH2)6-NH-CO-(CH2)4-CO-]n  +  n H2O",
         "Type: Condensation, Thermoplastic | Uses: Ropes, parachutes, tyre cords, toothbrush bristles, stockings"),

        ("Teflon (PTFE)", TEAL, LIGHT_TEAL,
         "Tetrafluoroethylene (CF2=CF2)",
         "n CF2=CF2  --[catalyst/pressure]-->  (-CF2-CF2-)n",
         "Type: Addition, Thermoplastic | Properties: Non-stick, chemically inert, withstands -200°C to +260°C | Uses: Non-stick cookware, gaskets, electrical insulation, medical implants"),

        ("Bakelite", ORANGE, LIGHT_ORG,
         "Phenol  +  Formaldehyde (in 2:1 ratio, acidic catalyst)",
         "Stage 1: Phenol + HCHO --> Novolac (linear, soluble)\nStage 2: Novolac + more HCHO + heat --> Bakelite (3D cross-linked)",
         "Type: Condensation, Thermosetting | Uses: Electrical switches, plugs, radio cabinets, billiard balls, handles"),

        ("Kevlar", PURPLE, LIGHT_PURP,
         "p-Phenylenediamine  +  Terephthaloyl chloride",
         "n H2N-C6H4-NH2  +  n ClCO-C6H4-COCl  -->  [-NH-C6H4-NH-CO-C6H4-CO-]n  +  n HCl",
         "Type: Condensation, Thermoplastic | Properties: 5x stronger than steel by weight | Uses: Bullet-proof vests, helmets, aircraft parts, sports equipment"),

        ("Buna-S (SBR)", RED, LIGHT_RED,
         "Butadiene  +  Styrene  (75:25 ratio)",
         "n CH2=CH-CH=CH2  +  n C6H5-CH=CH2  --[peroxide catalyst]-->  Buna-S copolymer",
         "Type: Addition copolymer | Uses: Car tyres (most common use), footwear, conveyor belts"),

        ("Buna-N (NBR)", colors.HexColor("#37474f"), colors.HexColor("#eceff1"),
         "Butadiene  +  Acrylonitrile  (60:40 ratio)",
         "n CH2=CH-CH=CH2  +  n CH2=CH-CN  --[catalyst]-->  Buna-N copolymer",
         "Properties: Oil-resistant, fuel-resistant | Uses: Oil seals, hoses, gaskets, gloves"),
    ]

    for name, col, bg_c, monomer, rxn_text, notes in polymers:
        story.append(KeepTogether([
            infobox(f"<b>{name}</b><br/>Monomer: {monomer}", bg=bg_c, bc=col),
            sp(3),
            rxnbox([rxn_text] if "\n" not in rxn_text else rxn_text.split("\n")),
            sp(3),
            infobox(notes, bg=GREY_BG, bc=BORDER),
            sp(8),
        ]))

    story.append(PageBreak())

    # ════════════════════════════════════════════════════════════════════════
    # FINAL QUICK REVISION
    # ════════════════════════════════════════════════════════════════════════
    story.append(banner("📝  Quick Revision — All Key Points at a Glance", DARK_BLUE))
    story.append(sp(10))

    rev_data = [
        [Paragraph("<b>Topic</b>", tbl_hd), Paragraph("<b>Key 1-Liner to Remember</b>", tbl_hd)],
        ["Organometallic", "Direct C-Metal bond | Example: R-Mg-X, Pb(C2H5)4, LiAlH4"],
        ["Polymer", "Large molecule of repeating monomers | Degree of polymerization = n"],
        ["Addition Polymer", "C=C monomer | No by-product | Examples: PVC, Teflon, Polythene"],
        ["Condensation Polymer", "2 functional groups | By-product = H2O | Examples: Nylon, Dacron, Bakelite"],
        ["Thermoplastic", "Softens on heating, recyclable | PVC, Nylon, Polythene"],
        ["Thermosetting", "Hardens permanently, 3D network | Bakelite, Epoxy"],
        ["Conducting Polymer", "Conjugated system + doping = conductivity | PANI, Polyacetylene"],
        ["Biodegradable Polymer", "Degraded by microbes | PLA, PHA | Used in medicine & packaging"],
        ["Grignard Reagent", "R-Mg-X in dry ether | Nobel 1912 | Makes 1°, 2°, 3° alcohols & acids"],
        ["Nylon 6,6", "Diamine + Diacid → Condensation | Uses: ropes, tyre cords, parachutes"],
        ["Teflon", "CF2=CF2 → Addition | Non-stick, chemically inert, -200 to +260°C"],
        ["Bakelite", "Phenol + HCHO → 2-stage → Thermosetting | Uses: electrical fittings"],
        ["Kevlar", "5x stronger than steel | Bullet-proof vests | Condensation polymer"],
        ["Buna-S", "Butadiene + Styrene | Car tyres"],
        ["Buna-N", "Butadiene + Acrylonitrile | Oil-resistant seals & hoses"],
    ]
    story.append(grid_table(rev_data, [5*cm, 12*cm], DARK_BLUE))
    story.append(sp(12))

    footer = Table([[Paragraph(
        "<b>Best of Luck for Your AKTU End Semester Exam!</b><br/>"
        "All topics explained simply | Unit 5 — BAS102 Engineering Chemistry",
        S("FT", fontSize=11, textColor=WHITE, fontName="Helvetica-Bold",
          alignment=TA_CENTER, leading=18)
    )]], colWidths=[17*cm])
    footer.setStyle(TableStyle([
        ("BACKGROUND",    (0,0),(-1,-1), DARK_BLUE),
        ("TOPPADDING",    (0,0),(-1,-1), 14),
        ("BOTTOMPADDING", (0,0),(-1,-1), 14),
    ]))
    story.append(footer)

    doc.build(story)
    print("Done:", out)

build()
