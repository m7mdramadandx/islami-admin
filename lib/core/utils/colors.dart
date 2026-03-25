import 'package:flutter/material.dart';
import 'package:islami_admin/core/theme/theme_controller.dart';

class AppPalette {
  final Color colorPrimary;
  final Color colorSecondary;
  final Color colorAccent;
  final Color colorBackground;
  final Color popupBackground;
  final Color buttonSecondary;
  final Color popUp;
  final Color textButton;
  final Color grey;
  final Color border;
  final Color dimmed;
  final Color transparent;
  final Color silverGrey;
  final Color whiteGrey;
  final Color whiteSilver;
  final Color greySilver;
  final Color natural50;
  final Color natural100;
  final Color natural200;
  final Color natural300;
  final Color natural400;
  final Color natural500;
  final Color natural600;
  final Color natural700;
  final Color natural800;
  final Color natural900;
  final Color text;
  final Color subText;
  final Color titleColor;
  final Color gold;
  final Color red;
  final Color blue;
  final Color green;
  final Color failureRed;
  final Color success;
  final Color info;
  final Color shimmer;
  final Color blackWhite;
  final Color whiteBlack;
  final Color whiteSolid;
  final Color blackSolid;

  const AppPalette({
    required this.colorPrimary,
    required this.colorSecondary,
    required this.colorAccent,
    required this.colorBackground,
    required this.popupBackground,
    required this.buttonSecondary,
    required this.popUp,
    required this.textButton,
    required this.grey,
    required this.border,
    required this.dimmed,
    required this.transparent,
    required this.silverGrey,
    required this.whiteGrey,
    required this.whiteSilver,
    required this.greySilver,
    required this.natural50,
    required this.natural100,
    required this.natural200,
    required this.natural300,
    required this.natural400,
    required this.natural500,
    required this.natural600,
    required this.natural700,
    required this.natural800,
    required this.natural900,
    required this.text,
    required this.subText,
    required this.titleColor,
    required this.gold,
    required this.red,
    required this.blue,
    required this.green,
    required this.failureRed,
    required this.success,
    required this.info,
    required this.shimmer,
    required this.blackWhite,
    required this.whiteBlack,
    required this.whiteSolid,
    required this.blackSolid,
  });

  static const light = AppPalette(
    colorPrimary: Color(0xFF4A6B52),
    colorSecondary: Color(0xFF052A22),
    colorAccent: Color(0xFFFFBA00),
    colorBackground: Color(0xFFF7F4EF),
    popupBackground: Color(0xFFFFFCF9),
    buttonSecondary: Color(0xFFEEE9E0),
    popUp: Color(0xFFFFFCF9),
    textButton: Color(0xFF4A6B52),
    grey: Color(0xFF5C5A56),
    border: Color(0xFFE5E0D6),
    dimmed: Color(0xFFEEE9E0),
    transparent: Color(0x00000000),
    silverGrey: Color(0xFFEEE9E0),
    whiteGrey: Color(0xFFFFFFFF),
    whiteSilver: Color(0xFFFFFFFF),
    greySilver: Color(0xFF4A4742),
    natural50: Color(0xFFFFFCF9),
    natural100: Color(0xFFF7F4EF),
    natural200: Color(0xFFEEE9E0),
    natural300: Color(0xFFD9D4CB),
    natural400: Color(0xFFA39E94),
    natural500: Color(0xFF6B6660),
    natural600: Color(0xFF4A4742),
    natural700: Color(0xFF383532),
    natural800: Color(0xFF242220),
    natural900: Color(0xFF141211),
    text: Color(0xFF242220),
    subText: Color(0xFF5C5A56),
    titleColor: Color(0xFF052A22),
    gold: Color(0xFFFFBA00),
    red: Color(0xFFD72323),
    blue: Color(0xFF3B82F6),
    green: Color(0xFF1F612A),
    failureRed: Color(0xFFB00020),
    success: Color(0xFF198754),
    info: Color(0xFF3B82F6),
    shimmer: Color(0xFFD9D4CB),
    blackWhite: Color(0xFF141211),
    whiteBlack: Color(0xFFFFFFFF),
    whiteSolid: Color(0xFFFFFFFF),
    blackSolid: Color(0xFF000000),
  );

  static const dark = AppPalette(
    colorPrimary: Color(0xFF5A7F62),
    colorSecondary: Color(0xFF052A22),
    colorAccent: Color(0xFFFFBA00),
    colorBackground: Color(0xFF081B15),
    popupBackground: Color(0xFF0F352C),
    buttonSecondary: Color(0xFF18423A),
    popUp: Color(0xFF0F352C),
    textButton: Color(0xFF5A7F62),
    grey: Color(0xFFA8B8B2),
    border: Color(0xFF245447),
    dimmed: Color(0xFF18423A),
    transparent: Color(0x00000000),
    silverGrey: Color(0xFF18423A),
    whiteGrey: Color(0xFF0F352C),
    whiteSilver: Color(0xFFE8F0ED),
    greySilver: Color(0xFFC5D1CC),
    natural50: Color(0xFF040F0C),
    natural100: Color(0xFF052A22),
    natural200: Color(0xFF0F352C),
    natural300: Color(0xFF18423A),
    natural400: Color(0xFF3D5E54),
    natural500: Color(0xFF7A9188),
    natural600: Color(0xFFA8B8B2),
    natural700: Color(0xFFC5D1CC),
    natural800: Color(0xFFE0E8E5),
    natural900: Color(0xFFF5F9F7),
    text: Color(0xFFF5F9F7),
    subText: Color(0xFFA8B8B2),
    titleColor: Color(0xFFE0E8E5),
    gold: Color(0xFFFFBA00),
    red: Color(0xFFD72323),
    blue: Color(0xFF445DEB),
    green: Color(0xFF1F612A),
    failureRed: Color(0xFFAC0303),
    success: Color(0xFF5A7F62),
    info: Color(0xFFA8B8B2),
    shimmer: Color(0xFF3D5E54),
    blackWhite: Color(0xFFF5F9F7),
    whiteBlack: Color(0xFF040F0C),
    whiteSolid: Color(0xFFFFFFFF),
    blackSolid: Color(0xFF000000),
  );
}

class AppColors {
  static AppPalette get _palette =>
      ThemeController.isDark ? AppPalette.dark : AppPalette.light;

  static Color get colorPrimary => _palette.colorPrimary;

  static Color get colorSecondary => _palette.colorSecondary;

  static Color get colorAccent => _palette.colorAccent;

  static Color get colorBackground => _palette.colorBackground;

  static Color get popupBackground => _palette.popupBackground;

  static Color get btnColor => _palette.colorPrimary;

  static Color get buttonSecondary => _palette.buttonSecondary;

  static Color get popUp => _palette.popUp;

  static Color get textButton => _palette.textButton;

  static Color get grey => _palette.grey;

  static Color get border => _palette.border;

  static Color get dimmed => _palette.dimmed;

  static Color get transparent => _palette.transparent;

  static Color get silverGrey => _palette.silverGrey;

  static Color get whiteGrey => _palette.whiteGrey;

  static Color get whiteSilver => _palette.whiteSilver;

  static Color get greySilver => _palette.greySilver;

  static Color get natural50 => _palette.natural50;

  static Color get natural100 => _palette.natural100;

  static Color get natural200 => _palette.natural200;

  static Color get natural300 => _palette.natural300;

  static Color get natural400 => _palette.natural400;

  static Color get natural500 => _palette.natural500;

  static Color get natural600 => _palette.natural600;

  static Color get natural700 => _palette.natural700;

  static Color get natural800 => _palette.natural800;

  static Color get natural900 => _palette.natural900;

  static Color get text => _palette.text;

  static Color get subText => _palette.subText;

  static Color get titleColor => _palette.titleColor;

  static Color get gold => _palette.gold;

  static Color get red => _palette.red;

  static Color get blue => _palette.blue;

  static Color get green => _palette.green;

  static Color get failureRed => _palette.failureRed;

  static Color get success => _palette.success;

  static Color get info => _palette.info;

  static Color get shimmer => _palette.shimmer;

  static Color get blackWhite => _palette.blackWhite;

  static Color get whiteBlack => _palette.whiteBlack;

  static Color get whiteSolid => _palette.whiteSolid;

  static Color get blackSolid => _palette.blackSolid;
}
