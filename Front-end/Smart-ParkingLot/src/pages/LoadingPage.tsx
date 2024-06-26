import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import loadingGif from '../assets/animation.gif';

export default function LoadingPage() {
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const timer = setTimeout(() => {
      const searchParams = new URLSearchParams(location.search);
      const address = searchParams.get('address');
      console.log(address);

      navigate(`/results?address=${address}`);
    }, 3000);

    return () => clearTimeout(timer);
  }, [location, navigate]);

  return (
    <motion.div
      className="min-h-screen flex flex-col items-center justify-center bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100 p-4"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <img src={loadingGif} alt="Loading" className="w-40 h-35" />
      <p className="text-xl mt-4">Picking the best spot for you...</p>
    </motion.div>
  );
}