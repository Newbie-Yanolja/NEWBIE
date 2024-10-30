/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        kbogothicbold: ['KBOGothic-Bold'],
        kbogothicmedium: ['KBOGothic-Medium'],
        kbogothiclight: ['KBOGothic-Light'],
      },
      colors: {
        white: '#FFFFFF',
        gray: {
          100: '#F7F7F7',
          200: '#D9D9D9',
          300: '#9B9BA3',
          400: '#66666D',
          500: '#3F3F47',
          600: '#202025',
          700: '#0F1012' /* main */,
        },
        green: {
          50: '#E7F6EE',
          100: '#C5E9D5',
          200: '#A1DABB',
          300: '#79CDA0',
          400: '#5BC28C',
          500: '#3AB778',
          600: '#33A76D',
          700: '#2B9560',
          800: '#268354',
          900: '#1E633F' /* main */,
        },
        success: {
          200: '#7FAAFF',
          400: '#4879FF',
        },
        warning: '#FFA600',
        error: {
          200: '#FFAEC5',
          400: '#FF5168',
        },
      },
    },
  },
  plugins: [],
};
