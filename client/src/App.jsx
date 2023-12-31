import "./app.scss";
import { createBrowserRouter, Outlet, RouterProvider } from "react-router-dom";
import React from "react";
import Navbar from "./components/navbar/Navbar";
import Footer from "./components/footer/Footer";
import Home from "./pages/home/Home";
import Gigs from "./pages/gigs/Gigs";
import Gig from "./pages/gig/Gig";
import Login from "./pages/login/Login";
import Register from "./pages/register/Register";
import Add from "./pages/add/Add";
import Edit from "./pages/edit/Edit";
import Orders from "./pages/orders/Orders";
import Messages from "./pages/messages/Messages";
import Message from "./pages/message/Message";
import MyGigs from "./pages/myGigs/MyGigs";
import Profile from "./pages/profile/Profile";
import Payment from "./pages/Payment/Payment";
import NotFound from "./components/errors/NotFound";
import { ChakraProvider } from "@chakra-ui/react";
import PaymentForm from "./components/paymenSucces/PaymentForm";

function App() {
  const Layout = () => {
    return (
      <div className="app">
        <Navbar />
        <Outlet />
        <ChakraProvider>
          <Footer />
        </ChakraProvider>
      </div>
    );
  };

  const router = createBrowserRouter([
    {
      path: "/",
      element: <Layout />,
      children: [
        {
          path: "/",
          element: <Home />,
        },
        {
          path: "/gigs",
          element: <Gigs />,
        },
        {
          path: "/myGigs",
          element: <MyGigs />,
        },
        {
          path: "/orders",
          element: <Orders />,
        },
        {
          path: "/messages",
          element: <Messages />,
        },
        {
          path: "/message/:id",
          element: <Message />,
        },
        {
          path: "/add",
          element: <Add />,
        },
        {
          path: "edit/:id",
          element: <Edit />,
        },
        {
          path: "/gig/:id",
          element: <Gig />,
        },
        {
          path: "/payment/:sessionId",
          element: (
            <ChakraProvider>
              <Payment />
            </ChakraProvider>
          ),
        },
        {
          path: "*",
          element: (
            <ChakraProvider>
              <NotFound />
            </ChakraProvider>
          ),
        },
      ],
    },

    {
      path: "/register",
      element: <Register />,
    },
    {
      path: "/login",
      element: <Login />,
    },
    {
      path: "/profile",
      element: <Profile />,
    },
    {
      path: "/payment-form/:id",
      element: (
        <ChakraProvider>
          <PaymentForm />
        </ChakraProvider>
      ),
    },
  ]);

  return <RouterProvider router={router} />;
}

export default App;
